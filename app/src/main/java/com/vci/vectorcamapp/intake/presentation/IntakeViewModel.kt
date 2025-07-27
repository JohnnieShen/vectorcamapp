package com.vci.vectorcamapp.intake.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SiteRepository
import com.vci.vectorcamapp.core.domain.repository.SurveillanceFormRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.errorOrNull
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.intake.domain.repository.LocationRepository
import com.vci.vectorcamapp.intake.domain.strategy.SurveillanceFormWorkflow
import com.vci.vectorcamapp.intake.domain.strategy.SurveillanceFormWorkflowFactory
import com.vci.vectorcamapp.intake.domain.use_cases.ValidationUseCases
import com.vci.vectorcamapp.intake.domain.util.IntakeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class IntakeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val validationUseCases: ValidationUseCases,
    private val deviceCache: DeviceCache,
    private val currentSessionCache: CurrentSessionCache,
    private val siteRepository: SiteRepository,
    private val surveillanceFormRepository: SurveillanceFormRepository,
    private val sessionRepository: SessionRepository,
    private val locationRepository: LocationRepository,
) : CoreViewModel() {

    companion object {
        private const val LOCATION_TIMEOUT_MS = 30000L
    }

    @Inject
    lateinit var transactionHelper: TransactionHelper

    @Inject
    lateinit var surveillanceFormWorkflowFactory: SurveillanceFormWorkflowFactory
    private lateinit var surveillanceFormWorkflow: SurveillanceFormWorkflow

    private val _state = MutableStateFlow(IntakeState())
    val state: StateFlow<IntakeState> = _state.onStart {
        loadFormDetails()
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000L), IntakeState()
    )

    private val _events = Channel<IntakeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: IntakeAction) {
        viewModelScope.launch {
            when (action) {
                IntakeAction.ReturnToLandingScreen -> {
                    currentSessionCache.clearSession()
                    _events.send(IntakeEvent.NavigateBackToLandingScreen)
                }

                IntakeAction.SubmitIntakeForm -> {
                    val session = _state.value.session
                    val surveillanceForm = _state.value.surveillanceForm

                    val collectorTitleResult =
                        validationUseCases.validateCollectorTitle(session.collectorTitle)
                    val collectorNameResult =
                        validationUseCases.validateCollectorName(session.collectorName)
                    val districtResult =
                        validationUseCases.validateDistrict(_state.value.selectedDistrict)
                    val sentinelSiteResult =
                        validationUseCases.validateSentinelSite(_state.value.selectedSentinelSite)
                    val houseNumberResult =
                        validationUseCases.validateHouseNumber(session.houseNumber)
                    val llinTypeResult =
                        surveillanceForm?.llinType?.let { validationUseCases.validateLlinType(it) }
                    val llinBrandResult =
                        surveillanceForm?.llinBrand?.let { validationUseCases.validateLlinBrand(it) }
                    val collectionDateResult =
                        validationUseCases.validateCollectionDate(session.collectionDate)
                    val collectionMethodResult =
                        validationUseCases.validateCollectionMethod(session.collectionMethod)
                    val specimenConditionResult =
                        validationUseCases.validateSpecimenCondition(session.specimenCondition)

                    _state.update {
                        it.copy(
                            intakeErrors = it.intakeErrors.copy(
                                collectorTitle = collectorTitleResult.errorOrNull(),
                                collectorName = collectorNameResult.errorOrNull(),
                                district = districtResult.errorOrNull(),
                                sentinelSite = sentinelSiteResult.errorOrNull(),
                                houseNumber = houseNumberResult.errorOrNull(),
                                llinType = llinTypeResult?.errorOrNull(),
                                llinBrand = llinBrandResult?.errorOrNull(),
                                collectionDate = collectionDateResult.errorOrNull(),
                                collectionMethod = collectionMethodResult.errorOrNull(),
                                specimenCondition = specimenConditionResult.errorOrNull()
                            )
                        )
                    }

                    val hasError = listOf(
                        collectorTitleResult,
                        collectorNameResult,
                        districtResult,
                        sentinelSiteResult,
                        houseNumberResult,
                        llinTypeResult,
                        llinBrandResult,
                        collectionDateResult,
                        collectionMethodResult,
                        specimenConditionResult
                    ).any { it is Result.Error }

                    if (!hasError) {
                        val selectedSite = _state.value.allSitesInProgram.find {
                            it.district == _state.value.selectedDistrict && it.sentinelSite == _state.value.selectedSentinelSite
                        }
                        if (selectedSite == null) {
                            emitError(IntakeError.SITE_NOT_FOUND)
                            return@launch
                        }

                        val success = transactionHelper.runAsTransaction {
                            val sessionResult =
                                sessionRepository.upsertSession(session, selectedSite.id)
                            sessionResult.onError { error ->
                                emitError(error)
                                return@runAsTransaction false
                            }

                            val surveillanceFormResult = surveillanceForm?.let {
                                surveillanceFormRepository.upsertSurveillanceForm(
                                    surveillanceForm, session.localId
                                )
                            } ?: Result.Success(Unit)

                            surveillanceFormResult.onError { error ->
                                emitError(error)
                                return@runAsTransaction false
                            }
                            true
                        }

                        if (success) {
                            currentSessionCache.saveSession(session, selectedSite.id)
                            _events.send(IntakeEvent.NavigateToImagingScreen)
                        }
                    }
                }

                is IntakeAction.EnterCollectorTitle -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectorTitle = action.text
                            )
                        )
                    }
                }

                is IntakeAction.EnterCollectorName -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectorName = action.text
                            )
                        )
                    }
                }

                is IntakeAction.SelectDistrict -> {
                    _state.update {
                        it.copy(
                            selectedDistrict = action.district, selectedSentinelSite = ""
                        )
                    }
                }

                is IntakeAction.SelectSentinelSite -> {
                    _state.update {
                        it.copy(
                            selectedSentinelSite = action.sentinelSite
                        )
                    }
                }

                is IntakeAction.EnterHouseNumber -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                houseNumber = action.text
                            )
                        )
                    }
                }

                is IntakeAction.EnterNumPeopleSleptInHouse -> {
                    val numPeopleSleptInHouse =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    numPeopleSleptInHouse?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm?.copy(
                                    numPeopleSleptInHouse = count
                                )
                            )
                        }
                    }
                }

                is IntakeAction.ToggleIrsConducted -> {
                    val wasIrsConducted = action.isChecked
                    _state.update {
                        it.copy(
                            surveillanceForm = it.surveillanceForm?.copy(
                                wasIrsConducted = wasIrsConducted,
                                monthsSinceIrs = if (wasIrsConducted) 0 else null
                            )
                        )
                    }
                }

                is IntakeAction.EnterMonthsSinceIrs -> {
                    val monthsSinceIrs =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    monthsSinceIrs?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm?.copy(
                                    monthsSinceIrs = count
                                )
                            )
                        }
                    }
                }

                is IntakeAction.EnterNumLlinsAvailable -> {
                    val numLlinsAvailable =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    numLlinsAvailable?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm?.copy(
                                    numLlinsAvailable = count
                                )
                            )
                        }
                        if (numLlinsAvailable == 0) {
                            _state.update {
                                it.copy(
                                    surveillanceForm = it.surveillanceForm?.copy(
                                        llinType = null,
                                        llinBrand = null,
                                        numPeopleSleptUnderLlin = null
                                    )
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    surveillanceForm = it.surveillanceForm?.copy(
                                        llinType = "", llinBrand = "", numPeopleSleptUnderLlin = 0
                                    )
                                )
                            }
                        }
                    }
                }

                is IntakeAction.SelectLlinType -> {
                    _state.update {
                        it.copy(
                            surveillanceForm = it.surveillanceForm?.copy(
                                llinType = action.option.label
                            )
                        )
                    }
                }

                is IntakeAction.SelectLlinBrand -> {
                    _state.update {
                        it.copy(
                            surveillanceForm = it.surveillanceForm?.copy(
                                llinBrand = action.option.label
                            )
                        )
                    }
                }

                is IntakeAction.EnterNumPeopleSleptUnderLlin -> {
                    val numPeopleSleptUnderLlin =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    numPeopleSleptUnderLlin?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm?.copy(
                                    numPeopleSleptUnderLlin = count
                                )
                            )
                        }
                    }
                }

                is IntakeAction.PickCollectionDate -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectionDate = action.date
                            )
                        )
                    }
                }

                is IntakeAction.SelectCollectionMethod -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectionMethod = action.option.label
                            )
                        )
                    }
                }

                is IntakeAction.SelectSpecimenCondition -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                specimenCondition = action.option.label
                            )
                        )
                    }
                }

                is IntakeAction.EnterNotes -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                notes = action.text
                            )
                        )
                    }
                }

                is IntakeAction.RetryLocation -> {
                    _state.update { it.copy(locationError = null) }
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                latitude = null, longitude = null
                            )
                        )
                    }
                    getLocation()
                }
            }
        }
    }

    private fun loadFormDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val programId = deviceCache.getProgramId()
            if (programId == null) {
                emitError(IntakeError.PROGRAM_NOT_FOUND)
                _events.send(IntakeEvent.NavigateBackToRegistrationScreen)
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            val currentSession = currentSessionCache.getSession()
            val allSites = siteRepository.getAllSitesByProgramId(programId)

            var savedForm: SurveillanceForm? = null
            var district = ""
            var sentinelSite = ""

            if (currentSession != null) {
                savedForm =
                    surveillanceFormRepository.getSurveillanceFormBySessionId(currentSession.localId)

                val siteId = currentSessionCache.getSiteId()
                val site = allSites.find { it.id == siteId }
                if (site != null) {
                    district = site.district
                    sentinelSite = site.sentinelSite
                }
            }

            val effectiveSession = currentSession ?: _state.value.session.copy(
                type = savedStateHandle.get<SessionType>("sessionType")
                    ?: SessionType.SURVEILLANCE
            )
            surveillanceFormWorkflow = surveillanceFormWorkflowFactory.create(effectiveSession.type)

            _state.update {
                it.copy(
                    isLoading = false,
                    session = effectiveSession,
                    surveillanceForm = savedForm ?: surveillanceFormWorkflow.getSurveillanceForm(),
                    allSitesInProgram = allSites,
                    selectedDistrict = district,
                    selectedSentinelSite = sentinelSite
                )
            }

            getLocation()
        }
    }

    private suspend fun getLocation() {
        if (_state.value.session.latitude == null || _state.value.session.longitude == null) {
            val locationResult = try {
                withTimeout(LOCATION_TIMEOUT_MS) {
                    locationRepository.getCurrentLocation()
                }
            } catch (e: SecurityException) {
                Result.Error(IntakeError.LOCATION_PERMISSION_DENIED)
            } catch (e: TimeoutCancellationException) {
                Result.Error(IntakeError.LOCATION_GPS_TIMEOUT)
            } catch (e: Exception) {
                Result.Error(IntakeError.UNKNOWN_ERROR)
            }

            locationResult.onSuccess { location ->
                _state.update {
                    it.copy(
                        session = it.session.copy(
                            latitude = location.latitude.toFloat(),
                            longitude = location.longitude.toFloat(),
                        ), locationError = null
                    )
                }
            }.onError { error ->
                if (error == IntakeError.LOCATION_GPS_TIMEOUT) {
                    _state.update { it.copy(locationError = error) }
                } else {
                    emitError(error)
                }
            }
        }
    }
}
