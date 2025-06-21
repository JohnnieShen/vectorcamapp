package com.vci.vectorcamapp.complete_session.form.presentation

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
class CompleteSessionFormViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CompleteSessionFormState())
    val state: StateFlow<CompleteSessionFormState> = _state

    fun onAction(action: CompleteSessionFormAction) {
        when (action) {
            is CompleteSessionFormAction.LoadSession -> loadSession(action.sessionId)
        }
    }

    private fun loadSession(sessionId: UUID) {
        viewModelScope.launch {
            val sessionAndSite = sessionRepository.getSessionAndSiteById(sessionId)
            val sessionAndSurveillanceForm = sessionRepository.getSessionAndSurveillanceForm(sessionId)

            when {
                sessionAndSite == null -> {
                    _state.update {
                        it.copy(error = "No site data found for session")
                    }
                }

                sessionAndSurveillanceForm == null -> {
                    _state.update {
                        it.copy(
                            session = sessionAndSite.session,
                            site = sessionAndSite.site,
                            surveillanceForm = null,
                            error = null
                        )
                    }
                }

                else -> {
                    _state.update {
                        it.copy(
                            session = sessionAndSite.session,
                            site = sessionAndSite.site,
                            surveillanceForm = sessionAndSurveillanceForm.surveillanceForm,
                            error = null
                        )
                    }
                }
            }
        }
    }
}
