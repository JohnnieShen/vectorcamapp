package com.vci.vectorcamapp.complete_session.specimens.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CompleteSessionSpecimensViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CompleteSessionSpecimensState())
    val state: StateFlow<CompleteSessionSpecimensState> = _state

    fun onAction(action: CompleteSessionSpecimensAction) {
        when (action) {
            is CompleteSessionSpecimensAction.LoadSession -> loadSession(action.sessionId)
        }
    }

    private fun loadSession(sessionId: UUID) {
        viewModelScope.launch {
            val result = sessionRepository.getSessionWithSpecimens(sessionId)
            if (result == null) {
                _state.update { it.copy(error = "No data found for session") }
            } else {
                _state.update {
                    it.copy(
                        session = result.session,
                        specimens = result.specimens,
                        error = null
                    )
                }
            }
        }
    }
}