package com.vci.vectorcamapp.complete_session.form.presentation

import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.complete_session.domain.util.CompleteSessionError
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
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
) : CoreViewModel() {

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
            if (sessionAndSite == null) {
                emitError(CompleteSessionError.SESSION_OR_SITE_NOT_FOUND)
                return@launch
            }

            // TODO: Surveillance form will be optional
            val sessionAndSurveillanceForm =
                sessionRepository.getSessionAndSurveillanceForm(sessionId)
            if (sessionAndSurveillanceForm == null) {
                emitError(CompleteSessionError.SESSION_OR_SURVEILLANCE_FORM_NOT_FOUND)
                return@launch
            }

            _state.update {
                it.copy(
                    session = sessionAndSite.session,
                    site = sessionAndSite.site,
                    surveillanceForm = sessionAndSurveillanceForm.surveillanceForm
                )
            }
        }
    }
}
