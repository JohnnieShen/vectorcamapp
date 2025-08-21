package com.vci.vectorcamapp.incomplete_session.presentation

import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.incomplete_session.domain.util.IncompleteSessionError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncompleteSessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val currentSessionCache: CurrentSessionCache
) : CoreViewModel() {

    private val _incompleteSessions = sessionRepository.observeIncompleteSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _state = MutableStateFlow(IncompleteSessionState())
    val state = combine(_incompleteSessions, _state) { incompleteSessions, state ->
        state.copy(
            sessions = incompleteSessions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), IncompleteSessionState())

    private val _events = Channel<IncompleteSessionEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: IncompleteSessionAction) {
        viewModelScope.launch {
            when (action) {
                is IncompleteSessionAction.ResumeSession -> {
                    try {
                        val sessionAndSite = sessionRepository.getSessionAndSiteById(action.sessionId)
                        if (sessionAndSite != null) {
                            currentSessionCache.saveSession(sessionAndSite.session, sessionAndSite.site.id)
                            _events.send(IncompleteSessionEvent.NavigateToIntakeScreen(sessionAndSite.session.type))
                        } else {
                            emitError(IncompleteSessionError.SESSION_NOT_FOUND)
                        }
                    } catch (e: Exception) {
                        emitError(IncompleteSessionError.SESSION_RETRIEVAL_FAILED)
                    }
                }

                is IncompleteSessionAction.ReturnToLandingScreen -> {
                    _events.send(IncompleteSessionEvent.NavigateBackToLandingScreen)
                }
            }
        }
    }
}
