package com.vci.vectorcamapp.complete_session.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.complete_session.details.domain.util.CompleteSessionDetailsError
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenWithSpecimenImagesAndInferenceResults
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.intake.presentation.IntakeEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CompleteSessionDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val specimenRepository: SpecimenRepository,
) : CoreViewModel() {

    private val _specimensWithImagesAndInferenceResults: Flow<List<SpecimenWithSpecimenImagesAndInferenceResults>> =
        flow {
            val sessionIdString = savedStateHandle.get<String>("sessionId")
            val sessionId = sessionIdString?.let { UUID.fromString(it) }
            if (sessionId == null) {
                emit(emptyList())
            } else {
                specimenRepository.observeSpecimenImagesAndInferenceResultsBySession(sessionId)
                    .collect { emit(it) }
            }
        }

    private val _state = MutableStateFlow(CompleteSessionDetailsState())
    val state: StateFlow<CompleteSessionDetailsState> = combine(
        _specimensWithImagesAndInferenceResults, _state
    ) { specimensWithImagesAndInferenceResults, state ->
        state.copy(
            specimensWithImagesAndInferenceResults = specimensWithImagesAndInferenceResults
        )
    }.onStart {
        loadCompleteSessionDetails()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CompleteSessionDetailsState())

    private val _events = Channel<CompleteSessionDetailsEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CompleteSessionDetailsAction) {
        viewModelScope.launch {
            when (action) {
                CompleteSessionDetailsAction.ReturnToCompleteSessionListScreen -> {
                    _events.send(CompleteSessionDetailsEvent.NavigateBackToCompleteSessionListScreen)
                }

                is CompleteSessionDetailsAction.ChangeSelectedTab -> {
                    _state.update { it.copy(selectedTab = action.selectedTab) }
                }
            }
        }
    }

    private fun loadCompleteSessionDetails() {
        viewModelScope.launch {
            val sessionId = savedStateHandle.get<String>("sessionId")?.let { UUID.fromString(it) }
            if (sessionId == null) {
                emitError(CompleteSessionDetailsError.SESSION_NOT_FOUND)
                return@launch
            }

            val sessionAndSite = sessionRepository.getSessionAndSiteById(sessionId)
            val sessionAndSurveillanceForm =
                sessionRepository.getSessionAndSurveillanceFormById(sessionId)

            val session = sessionAndSite?.session
            val site = sessionAndSite?.site
            val surveillanceForm = sessionAndSurveillanceForm?.surveillanceForm

            if (session == null) {
                emitError(CompleteSessionDetailsError.SESSION_NOT_FOUND)
                return@launch
            }

            if (site == null) {
                emitError(CompleteSessionDetailsError.SITE_NOT_FOUND)
                return@launch
            }

            _state.update {
                it.copy(
                    session = session,
                    site = site,
                    surveillanceForm = surveillanceForm,
                )
            }
        }
    }
}
