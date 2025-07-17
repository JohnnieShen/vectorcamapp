package com.vci.vectorcamapp.landing.presentation

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val deviceCache: DeviceCache,
    private val currentSessionCache: CurrentSessionCache,
    private val programRepository: ProgramRepository,
    sessionRepository: SessionRepository,
) : CoreViewModel() {

    private val _incompleteSessionsCount = sessionRepository.observeIncompleteSessions()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    private val _state = MutableStateFlow(LandingState())
    val state: StateFlow<LandingState> = combine(
        _state,
        _incompleteSessionsCount
    ) { state, incompleteSessionsCount ->
        state.copy(incompleteSessionsCount = incompleteSessionsCount)
    }.onStart {
        loadLandingDetails()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        LandingState()
    )

    private val _events = Channel<LandingEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LandingAction) {
        viewModelScope.launch {
            when (action) {
                LandingAction.StartNewSurveillanceSession -> {
                    _events.send(LandingEvent.NavigateToNewSurveillanceSessionScreen)
                }

                LandingAction.StartNewNonSurveillanceSession -> {
                    Log.d(
                        "Navigation", "StartNewNonSurveillanceSession"
                    )
                }

                LandingAction.ViewIncompleteSessions -> {
                    _events.send(LandingEvent.NavigateToIncompleteSessionsScreen)
                }

                LandingAction.ViewCompleteSessions -> {
                    _events.send(LandingEvent.NavigateToCompleteSessionsScreen)
                }

                LandingAction.ResumeSession -> {
                    _state.update { it.copy(showResumeDialog = false) }
                    _events.send(LandingEvent.NavigateToNewSurveillanceSessionScreen)
                }

                LandingAction.DismissResumePrompt -> {
                    currentSessionCache.clearSession()
                    _state.update { it.copy(showResumeDialog = false) }
                }
            }
        }
    }

    private fun loadLandingDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val programId = deviceCache.getProgramId()
            if (programId == null) {
                _events.send(LandingEvent.NavigateBackToRegistrationScreen)
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            val program = programRepository.getProgramById(programId)
            if (program == null) {
                _events.send(LandingEvent.NavigateBackToRegistrationScreen)
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            val currentSession = currentSessionCache.getSession()
            if (currentSession != null) {
                _state.update { it.copy(showResumeDialog = true) }
            }

            _state.update {
                it.copy(
                    enrolledProgram = program, isLoading = false
                )
            }
        }
    }
}

