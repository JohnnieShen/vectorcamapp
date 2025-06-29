package com.vci.vectorcamapp.imaging.presentation

import android.content.Context
import android.util.Log
import android.view.OrientationEventListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.data.upload.image.ImageUploadWorker
import com.vci.vectorcamapp.core.data.upload.metadata.MetadataUploadWorker
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.repository.BoundingBoxRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.imaging.domain.repository.CameraRepository
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import com.vci.vectorcamapp.imaging.presentation.extensions.cropToBoundingBoxAndPad
import com.vci.vectorcamapp.imaging.presentation.extensions.toUprightBitmap
import com.vci.vectorcamapp.imaging.presentation.model.composites.SpecimenAndBoundingBoxUi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ImagingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val currentSessionCache: CurrentSessionCache,
    private val sessionRepository: SessionRepository,
    private val specimenRepository: SpecimenRepository,
    private val boundingBoxRepository: BoundingBoxRepository,
    private val cameraRepository: CameraRepository,
    private val inferenceRepository: InferenceRepository
) : ViewModel() {

    @Inject
    lateinit var transactionHelper: TransactionHelper

    @OptIn(ExperimentalCoroutinesApi::class)
    private val specimensUiFlow: Flow<List<SpecimenAndBoundingBoxUi>> = flow {
        val session = currentSessionCache.getSession()
        emit(session)
    }.flatMapLatest { session ->
        if (session == null) {
            flowOf(emptyList())
        } else {
            specimenRepository.observeSpecimensAndBoundingBoxesBySession(session.localId)
                .map { relations ->
                    relations.map { relation ->
                        SpecimenAndBoundingBoxUi(
                            specimen = relation.specimen,
                            boundingBoxUi = inferenceRepository.convertToBoundingBoxUi(relation.boundingBox)
                        )
                    }
                }
        }
    }

    private val orientationListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(displayOrientation: Int) {
            val currentRotation = _state.value.displayOrientation
            if (currentRotation != displayOrientation) {
                _state.update { it.copy(displayOrientation = displayOrientation) }
            }
        }
    }

    private val _state = MutableStateFlow(ImagingState())
    val state: StateFlow<ImagingState> = combine(
        specimensUiFlow, _state
    ) { specimensUi, state ->
        state.copy(capturedSpecimensAndBoundingBoxesUi = specimensUi)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ImagingState()
    )

    private val _events = Channel<ImagingEvent>()
    val events = _events.receiveAsFlow()

    init {
        orientationListener.enable()

        viewModelScope.launch {
            if (currentSessionCache.getSession() == null) {
                _events.send(ImagingEvent.NavigateBackToLandingScreen)
                _events.send(ImagingEvent.DisplayImagingError(ImagingError.NO_ACTIVE_SESSION))
            }
        }
    }

    fun onAction(action: ImagingAction) {
        viewModelScope.launch {
            when (action) {
                is ImagingAction.CorrectSpecimenId -> {
                    _state.update {
                        it.copy(
                            currentSpecimen = it.currentSpecimen.copy(
                                id = action.specimenId
                            )
                        )
                    }
                }

                is ImagingAction.ProcessFrame -> {
                    try {
                        val displayOrientation = _state.value.displayOrientation
                        val bitmap = action.frame.toUprightBitmap(displayOrientation)

                        val specimenId = inferenceRepository.readSpecimenId(bitmap)
                        val boundingBoxes = inferenceRepository.detectSpecimen(bitmap)

                        _state.update {
                            it.copy(
                                currentSpecimen = it.currentSpecimen.copy(id = specimenId),
                                previewBoundingBoxesUiList = boundingBoxes.map { boundingBox ->
                                    inferenceRepository.convertToBoundingBoxUi(
                                        boundingBox
                                    )
                                })
                        }
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Image processing setup failed: ${e.message}")
                    } finally {
                        action.frame.close()
                    }
                }

                ImagingAction.SaveSessionProgress -> {
                    currentSessionCache.clearSession()
                    _events.send(ImagingEvent.NavigateBackToLandingScreen)
                }

                ImagingAction.SubmitSession -> {
                    val currentSession = currentSessionCache.getSession()
                    val currentSessionSiteId = currentSessionCache.getSiteId()

                    if (currentSession == null || currentSessionSiteId == null) {
                        _events.send(ImagingEvent.NavigateBackToLandingScreen)
                        return@launch
                    }

                    val success = sessionRepository.markSessionAsComplete(currentSession.localId)
                    if (success) {
                        val uploadConstraints =
                            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()

                        val metadataUploadRequest =
                            OneTimeWorkRequestBuilder<MetadataUploadWorker>().setInputData(
                                workDataOf(
                                    "session_id" to currentSession.localId.toString(),
                                    "site_id" to currentSessionSiteId,
                                )
                            ).setConstraints(uploadConstraints).setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                WorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS,
                            ).build()

                        val imageUploadRequest =
                            OneTimeWorkRequestBuilder<ImageUploadWorker>().setInputData(workDataOf("session_id" to currentSession.localId.toString()))
                                .setConstraints(uploadConstraints).setBackoffCriteria(
                                    BackoffPolicy.LINEAR,
                                    WorkRequest.MIN_BACKOFF_MILLIS,
                                    TimeUnit.MILLISECONDS,
                                ).build()

                        WorkManager.getInstance(context).beginUniqueWork(
                            "metadata_upload_work",
                            ExistingWorkPolicy.REPLACE,
                            metadataUploadRequest
                        ).then(imageUploadRequest).enqueue()

                        currentSessionCache.clearSession()
                        _events.send(ImagingEvent.NavigateBackToLandingScreen)
                    }
                }

                is ImagingAction.CaptureImage -> {
                    _state.update { it.copy(isCapturing = true) }

                    val captureResult = cameraRepository.captureImage(action.controller)

                    _state.update { it.copy(isCapturing = false) }

                    captureResult.onSuccess { image ->
                        val displayOrientation = _state.value.displayOrientation
                        val bitmap = image.toUprightBitmap(displayOrientation)
                        image.close()

                        val boundingBoxesList = inferenceRepository.detectSpecimen(bitmap)

                        when (boundingBoxesList.size) {
                            0 -> {
                                _events.send(ImagingEvent.DisplayImagingError(ImagingError.NO_SPECIMEN_FOUND))
                            }

                            1 -> {
                                val boundingBox = boundingBoxesList[0]
                                val croppedAndPadded = bitmap.cropToBoundingBoxAndPad(boundingBox)
                                val (species, sex, abdomenStatus) = inferenceRepository.classifySpecimen(
                                    croppedAndPadded
                                )

                                _state.update {
                                    it.copy(
                                        currentSpecimen = it.currentSpecimen.copy(
                                            species = species?.label,
                                            sex = sex?.label,
                                            abdomenStatus = abdomenStatus?.label,
                                        ),
                                        currentImage = bitmap,
                                        captureBoundingBoxUi = inferenceRepository.convertToBoundingBoxUi(
                                            boundingBox
                                        ),
                                        previewBoundingBoxesUiList = emptyList()
                                    )
                                }
                            }

                            else -> {
                                _events.send(ImagingEvent.DisplayImagingError(ImagingError.MULTIPLE_SPECIMENS_FOUND))
                            }
                        }
                    }.onError { error ->
                        if (error == ImagingError.NO_ACTIVE_SESSION) {
                            _events.send(ImagingEvent.NavigateBackToLandingScreen)
                        }
                        _events.send(ImagingEvent.DisplayImagingError(error))
                    }
                }

                ImagingAction.RetakeImage -> {
                    clearCurrentSpecimenStateFields()
                }

                ImagingAction.SaveImageToSession -> {
                    val bitmap = _state.value.currentImage ?: return@launch
                    val specimenId = _state.value.currentSpecimen.id
                    val timestamp = System.currentTimeMillis()
                    val filename = buildString {
                        append(specimenId)
                        append("_")
                        append(timestamp)
                        append(".jpg")
                    }

                    val currentSession = currentSessionCache.getSession()
                    if (currentSession == null) {
                        _events.send(ImagingEvent.NavigateBackToLandingScreen)
                        return@launch
                    }

                    val saveResult = cameraRepository.saveImage(bitmap, filename, currentSession)

                    saveResult.onSuccess { imageUri ->
                        val specimen = Specimen(
                            id = specimenId,
                            species = _state.value.currentSpecimen.species,
                            sex = _state.value.currentSpecimen.sex,
                            abdomenStatus = _state.value.currentSpecimen.abdomenStatus,
                            imageUri = imageUri,
                            capturedAt = timestamp,
                            submittedAt = null
                        )

                        val success = transactionHelper.runAsTransaction {
                            val boundingBoxUi =
                                _state.value.captureBoundingBoxUi ?: return@runAsTransaction false
                            val boundingBox = inferenceRepository.convertToBoundingBox(
                                boundingBoxUi
                            )

                            val specimenResult =
                                specimenRepository.insertSpecimen(specimen, currentSession.localId)
                            val boundingBoxResult =
                                boundingBoxRepository.insertBoundingBox(boundingBox, specimen.id)

                            specimenResult.onError { error ->
                                Log.d("ROOM ERROR", "Specimen error: $error")
                            }

                            boundingBoxResult.onError { error ->
                                Log.d("ROOM ERROR", "Bounding box error: $error")
                            }

                            (specimenResult !is Result.Error) && (boundingBoxResult !is Result.Error)
                        }

                        if (success) {
                            clearCurrentSpecimenStateFields()
                        } else {
                            cameraRepository.deleteSavedImage(imageUri)
                        }
                    }.onError { error ->
                        _events.send(ImagingEvent.DisplayImagingError(error))
                    }
                }
            }
        }
    }

    private fun clearCurrentSpecimenStateFields() {
        _state.update {
            it.copy(
                currentSpecimen = it.currentSpecimen.copy(
                    id = "", species = null, sex = null, abdomenStatus = null
                ),
                currentImage = null,
                captureBoundingBoxUi = null,
                previewBoundingBoxesUiList = emptyList(),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        orientationListener.disable()
        inferenceRepository.closeResources()
    }
}
