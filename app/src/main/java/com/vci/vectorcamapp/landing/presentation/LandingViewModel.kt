package com.vci.vectorcamapp.landing.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val currentSessionCache: CurrentSessionCache,
) : ViewModel() {
    private val _state = MutableStateFlow(LandingState())
    val state: StateFlow<LandingState> = _state.onStart {
        loadCompleteAndIncompleteSessions()
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000L), LandingState()
    )

    private val _events = Channel<LandingEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (currentSessionCache.getSession() != null) {
                _state.update { it.copy(showResumeDialog = true) }
            }
        }
    }

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

    // TODO: Update to load from local database. Add Repository. Add error handling (and event receiver - refer to CryptoTracker example) and display onto UI.
    private fun loadCompleteAndIncompleteSessions() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            delay(3000L)
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }
}