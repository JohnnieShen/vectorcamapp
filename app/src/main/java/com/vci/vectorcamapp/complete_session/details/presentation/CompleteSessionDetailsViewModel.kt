package com.vci.vectorcamapp.complete_session.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.complete_session.details.domain.util.CompleteSessionDetailsError
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CompleteSessionDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
) : CoreViewModel() {

    private val _state = MutableStateFlow(CompleteSessionDetailsState())
    val state = _state.onStart {
        loadCompleteSessionDetails()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CompleteSessionDetailsState())

    fun onAction(action: CompleteSessionDetailsAction) {
        viewModelScope.launch {
            when (action) {
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
            val sessionAndSurveillanceForm = sessionRepository.getSessionAndSurveillanceFormById(sessionId)
            val sessionAndSpecimens = sessionRepository.getSessionWithSpecimensById(sessionId)

            val session = sessionAndSite?.session
            val site = sessionAndSite?.site
            val surveillanceForm = sessionAndSurveillanceForm?.surveillanceForm
            val specimens = sessionAndSpecimens?.specimens

            if (session == null) {
                emitError(CompleteSessionDetailsError.SESSION_NOT_FOUND)
                return@launch
            }

            if (site == null) {
                emitError(CompleteSessionDetailsError.SITE_NOT_FOUND)
                return@launch
            }

            if (surveillanceForm == null) {
                emitError(CompleteSessionDetailsError.SURVEILLANCE_FORM_NOT_FOUND)
                return@launch
            }

            if (specimens == null) {
                emitError(CompleteSessionDetailsError.SPECIMENS_NOT_FOUND)
                return@launch
            }

            _state.update {
                it.copy(
                    session = session,
                    site = site,
                    surveillanceForm = surveillanceForm,
                    specimens = specimens
                )
            }
        }
    }
}
