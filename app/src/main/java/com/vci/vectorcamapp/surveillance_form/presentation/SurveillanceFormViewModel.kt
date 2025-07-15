package com.vci.vectorcamapp.surveillance_form.presentation

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SiteRepository
import com.vci.vectorcamapp.core.domain.repository.SurveillanceFormRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.errorOrNull
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.surveillance_form.domain.repository.LocationRepository
import com.vci.vectorcamapp.surveillance_form.domain.use_cases.ValidationUseCases
import com.vci.vectorcamapp.surveillance_form.domain.util.SurveillanceFormError
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
class SurveillanceFormViewModel @Inject constructor(
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

    private val _state = MutableStateFlow(SurveillanceFormState())
    val state: StateFlow<SurveillanceFormState> = _state.onStart {
        loadFormDetails()
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000L), SurveillanceFormState()
    )

    private val _events = Channel<SurveillanceFormEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SurveillanceFormAction) {
        viewModelScope.launch {
            when (action) {
                SurveillanceFormAction.ReturnToLandingScreen -> {
                    currentSessionCache.clearSession()
                    _events.send(SurveillanceFormEvent.NavigateBackToLandingScreen)
                }

                SurveillanceFormAction.SubmitSurveillanceForm -> {
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
                        surveillanceForm.llinType?.let { validationUseCases.validateLlinType(it) }
                    val llinBrandResult =
                        surveillanceForm.llinBrand?.let { validationUseCases.validateLlinBrand(it) }
                    val collectionDateResult =
                        validationUseCases.validateCollectionDate(session.collectionDate)
                    val collectionMethodResult =
                        validationUseCases.validateCollectionMethod(session.collectionMethod)
                    val specimenConditionResult =
                        validationUseCases.validateSpecimenCondition(session.specimenCondition)

                    _state.update {
                        it.copy(
                            surveillanceFormErrors = it.surveillanceFormErrors.copy(
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
                            emitError(SurveillanceFormError.SITE_NOT_FOUND)
                            return@launch
                        }

                        val success = transactionHelper.runAsTransaction {
                            val sessionResult =
                                sessionRepository.upsertSession(session, selectedSite.id)
                            sessionResult.onError { error ->
                                emitError(error)
                                return@runAsTransaction false
                            }

                            val surveillanceFormResult =
                                surveillanceFormRepository.upsertSurveillanceForm(
                                    surveillanceForm, session.localId
                                )
                            surveillanceFormResult.onError { error ->
                                emitError(error)
                                return@runAsTransaction false
                            }
                            true
                        }

                        if (success) {
                            currentSessionCache.saveSession(session, selectedSite.id)
                            _events.send(SurveillanceFormEvent.NavigateToImagingScreen)
                        }
                    }
                }

                is SurveillanceFormAction.EnterCollectorTitle -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectorTitle = action.text
                            )
                        )
                    }
                }

                is SurveillanceFormAction.EnterCollectorName -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectorName = action.text
                            )
                        )
                    }
                }

                is SurveillanceFormAction.SelectDistrict -> {
                    _state.update {
                        it.copy(
                            selectedDistrict = action.option.label, selectedSentinelSite = ""
                        )
                    }
                }

                is SurveillanceFormAction.SelectSentinelSite -> {
                    _state.update {
                        it.copy(
                            selectedSentinelSite = action.option.label
                        )
                    }
                }

                is SurveillanceFormAction.EnterHouseNumber -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                houseNumber = action.text
                            )
                        )
                    }
                }

                is SurveillanceFormAction.EnterNumPeopleSleptInHouse -> {
                    val numPeopleSleptInHouse =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    numPeopleSleptInHouse?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm.copy(
                                    numPeopleSleptInHouse = count
                                )
                            )
                        }
                    }
                }

                is SurveillanceFormAction.ToggleIrsConducted -> {
                    val wasIrsConducted = action.isChecked
                    _state.update {
                        it.copy(
                            surveillanceForm = it.surveillanceForm.copy(
                                wasIrsConducted = wasIrsConducted,
                                monthsSinceIrs = if (wasIrsConducted) 0 else null
                            )
                        )
                    }
                }

                is SurveillanceFormAction.EnterMonthsSinceIrs -> {
                    val monthsSinceIrs =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    monthsSinceIrs?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm.copy(
                                    monthsSinceIrs = count
                                )
                            )
                        }
                    }
                }

                is SurveillanceFormAction.EnterNumLlinsAvailable -> {
                    val numLlinsAvailable =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    numLlinsAvailable?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm.copy(
                                    numLlinsAvailable = count
                                )
                            )
                        }
                        if (numLlinsAvailable == 0) {
                            _state.update {
                                it.copy(
                                    surveillanceForm = it.surveillanceForm.copy(
                                        llinType = null,
                                        llinBrand = null,
                                        numPeopleSleptUnderLlin = null
                                    )
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    surveillanceForm = it.surveillanceForm.copy(
                                        llinType = "", llinBrand = "", numPeopleSleptUnderLlin = 0
                                    )
                                )
                            }
                        }
                    }
                }

                is SurveillanceFormAction.SelectLlinType -> {
                    _state.update {
                        it.copy(
                            surveillanceForm = it.surveillanceForm.copy(
                                llinType = action.option.label
                            )
                        )
                    }
                }

                is SurveillanceFormAction.SelectLlinBrand -> {
                    _state.update {
                        it.copy(
                            surveillanceForm = it.surveillanceForm.copy(
                                llinBrand = action.option.label
                            )
                        )
                    }
                }

                is SurveillanceFormAction.EnterNumPeopleSleptUnderLlin -> {
                    val numPeopleSleptUnderLlin =
                        if (action.count.isBlank()) 0 else action.count.toIntOrNull()
                    numPeopleSleptUnderLlin?.let { count ->
                        _state.update {
                            it.copy(
                                surveillanceForm = it.surveillanceForm.copy(
                                    numPeopleSleptUnderLlin = count
                                )
                            )
                        }
                    }
                }

                is SurveillanceFormAction.PickCollectionDate -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectionDate = action.date
                            )
                        )
                    }
                }

                is SurveillanceFormAction.SelectCollectionMethod -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                collectionMethod = action.option.label
                            )
                        )
                    }
                }

                is SurveillanceFormAction.SelectSpecimenCondition -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                specimenCondition = action.option.label
                            )
                        )
                    }
                }

                is SurveillanceFormAction.EnterNotes -> {
                    _state.update {
                        it.copy(
                            session = it.session.copy(
                                notes = action.text
                            )
                        )
                    }
                }

                is SurveillanceFormAction.RetryLocation -> {
                    _state.update { it.copy(locationError = null) }
                    _state.update { it.copy(latitude = null, longitude = null) }
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
                emitError(SurveillanceFormError.MISSING_PROGRAM_ID)
                _events.send(SurveillanceFormEvent.NavigateBackToRegistrationScreen)
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
                Log.d("SAVED FORM", "Saved form: ${savedForm?.wasIrsConducted}")

                val siteId = currentSessionCache.getSiteId()
                val site = allSites.find { it.id == siteId }
                if (site != null) {
                    district = site.district
                    sentinelSite = site.sentinelSite
                }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    session = currentSession ?: it.session,
                    surveillanceForm = savedForm ?: it.surveillanceForm,
                    allSitesInProgram = allSites,
                    selectedDistrict = district,
                    selectedSentinelSite = sentinelSite
                )
            }

            getLocation()
        }
    }

    private suspend fun getLocation() {
        if (_state.value.latitude == null || _state.value.longitude == null) {
            val locationResult = try {
                withTimeout(LOCATION_TIMEOUT_MS) {
                    locationRepository.getCurrentLocation()
                }
            } catch (e: SecurityException) {
                Result.Error(SurveillanceFormError.LOCATION_PERMISSION_DENIED)
            } catch (e: TimeoutCancellationException) {
                Result.Error(SurveillanceFormError.LOCATION_GPS_TIMEOUT)
            } catch (e: Exception) {
                Result.Error(SurveillanceFormError.UNKNOWN_ERROR)
            }

            locationResult.onSuccess { location ->
                _state.update {
                    it.copy(
                        latitude = location.latitude.toFloat(),
                        longitude = location.longitude.toFloat(),
                        locationError = null
                    )
                }
            }.onError { error ->
                if (error == SurveillanceFormError.LOCATION_GPS_TIMEOUT) {
                    _state.update { it.copy(locationError = error) }
                } else {
                    emitError(error)
                }
            }
        }
    }
}
