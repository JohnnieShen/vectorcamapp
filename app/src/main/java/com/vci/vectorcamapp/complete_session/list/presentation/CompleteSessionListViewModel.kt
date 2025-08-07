package com.vci.vectorcamapp.complete_session.list.presentation

import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.domain.model.enums.UploadStatus
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.repository.WorkManagerRepository
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteSessionListViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val specimenRepository: SpecimenRepository,
    private val workManagerRepository: WorkManagerRepository
) : CoreViewModel() {

    private val _completeSessionsAndSites = sessionRepository.observeCompleteSessionsAndSites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(CompleteSessionListState())
    val state = combine(_completeSessionsAndSites, _state) { completeSessionsAndSites, state ->
        state.copy(sessionsAndSites = completeSessionsAndSites)
    }.onStart {
        loadCompleteSessionListDetails()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CompleteSessionListState())

    private val _events = Channel<CompleteSessionListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CompleteSessionListAction) {
        viewModelScope.launch {
            when (action) {
                CompleteSessionListAction.ReturnToLandingScreen -> {
                    _events.send(CompleteSessionListEvent.NavigateBackToLandingScreen)
                }

                is CompleteSessionListAction.ViewCompleteSessionDetails -> {
                    _events.send(CompleteSessionListEvent.NavigateToCompleteSessionDetails(action.sessionId))
                }

                is CompleteSessionListAction.UploadAllPendingSessions -> {
                    val completeSessionsAndSites =
                        sessionRepository.observeCompleteSessionsAndSites().first()
                    for (sessionAndSite in completeSessionsAndSites) {
                        val session = sessionAndSite.session
                        val site = sessionAndSite.site

                        val isSessionUploaded = (session.submittedAt != null)

                        val specimens =
                            specimenRepository.getSpecimenImagesAndInferenceResultsBySession(
                                session.localId
                            )
                        val areSpecimensUploaded = !specimens.any { specimen ->
                            specimen.specimenImagesAndInferenceResults.any { (image, _) ->
                                image.metadataUploadStatus != UploadStatus.COMPLETED || image.imageUploadStatus != UploadStatus.COMPLETED
                            }
                        }

                        if (isSessionUploaded && areSpecimensUploaded) continue

                        workManagerRepository.enqueueSessionUpload(session.localId, site.id)
                    }
                }
            }
        }
    }

    private fun loadCompleteSessionListDetails() {
        viewModelScope.launch {
            _completeSessionsAndSites.collect { sessionsAndSites ->
                val sessionIds = sessionsAndSites.map { it.session.localId }
                workManagerRepository.observeAnySessionUploadRunning(sessionIds)
                    .collect { isRunning ->
                        _state.update { it.copy(isUploading = isRunning) }
                    }
            }
        }
    }
}
