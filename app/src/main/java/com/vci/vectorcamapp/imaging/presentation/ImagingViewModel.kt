package com.vci.vectorcamapp.imaging.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.OrientationEventListener
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenWithSpecimenImagesAndInferenceResults
import com.vci.vectorcamapp.core.domain.model.enums.UploadStatus
import com.vci.vectorcamapp.core.domain.repository.InferenceResultRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenImageRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.repository.WorkManagerRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.imaging.domain.repository.CameraRepository
import com.vci.vectorcamapp.imaging.domain.strategy.ImagingWorkflow
import com.vci.vectorcamapp.imaging.domain.strategy.ImagingWorkflowFactory
import com.vci.vectorcamapp.imaging.domain.use_cases.ValidateSpecimenIdUseCase
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import com.vci.vectorcamapp.imaging.presentation.extensions.toUprightBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class ImagingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val currentSessionCache: CurrentSessionCache,
    private val sessionRepository: SessionRepository,
    private val specimenRepository: SpecimenRepository,
    private val specimenImageRepository: SpecimenImageRepository,
    private val inferenceResultRepository: InferenceResultRepository,
    private val cameraRepository: CameraRepository,
    private val workRepository: WorkManagerRepository,
    private val validateSpecimenIdUseCase: ValidateSpecimenIdUseCase,
) : CoreViewModel() {

    @Inject
    lateinit var transactionHelper: TransactionHelper

    @Inject
    lateinit var imagingWorkflowFactory: ImagingWorkflowFactory
    private lateinit var imagingWorkflow: ImagingWorkflow

    private val _specimensWithImagesAndInferenceResults: Flow<List<SpecimenWithSpecimenImagesAndInferenceResults>> =
        flow {
            val session = currentSessionCache.getSession()
            if (session == null) {
                emit(emptyList())
            } else {
                specimenRepository.observeSpecimenImagesAndInferenceResultsBySession(session.localId)
                    .collect { emit(it) }
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
        _specimensWithImagesAndInferenceResults, _state
    ) { specimensWithImagesAndInferenceResults, state ->
        state.copy(
            specimensWithImagesAndInferenceResults = specimensWithImagesAndInferenceResults,
            isLoading = false
        )
    }.onStart {
        loadImagingDetails()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ImagingState(isLoading = true)
    )

    private val _events = Channel<ImagingEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ImagingAction) {
        viewModelScope.launch {
            when (action) {
                ImagingAction.ShowExitDialog -> {
                    _state.update { it.copy(showExitDialog = true) }
                }

                ImagingAction.DismissExitDialog -> {
                    _state.update { it.copy(showExitDialog = false, pendingAction = null) }
                }

                is ImagingAction.SelectPendingAction -> {
                    _state.update { it.copy(pendingAction = action.pendingAction) }
                }

                ImagingAction.ClearPendingAction -> {
                    _state.update { it.copy(pendingAction = null) }
                }

                ImagingAction.ConfirmPendingAction -> {
                    val actionToConfirm = _state.value.pendingAction
                    _state.update { it.copy(showExitDialog = false, pendingAction = null) }
                    actionToConfirm?.let { onAction(it) }
                }

                is ImagingAction.ManualFocusAt -> {
                    _state.update { it.copy(manualFocusPoint = action.offset) }
                }

                is ImagingAction.CancelManualFocus -> {
                    _state.update { it.copy(manualFocusPoint = null) }
                }

                is ImagingAction.CorrectSpecimenId -> {
                    _state.update {
                        it.copy(
                            currentSpecimen = it.currentSpecimen.copy(id = action.specimenId)
                        )
                    }
                }

                is ImagingAction.ProcessFrame -> {
                    if (!_state.value.isCameraReady) {
                        _state.update { it.copy(isCameraReady = true) }
                    }

                    try {
                        val displayOrientation = _state.value.displayOrientation
                        val bitmap = action.frame.toUprightBitmap(displayOrientation)

                        val liveFrameProcessingResult = imagingWorkflow.processLiveFrame(bitmap)
                        validateSpecimenIdUseCase(liveFrameProcessingResult.specimenId, shouldAutoCorrect = true).onSuccess { correctedSpecimenId ->
                            _state.update {
                                it.copy(
                                    currentSpecimen = it.currentSpecimen.copy(id = correctedSpecimenId),
                                )
                            }
                        }

                        _state.update {
                            it.copy(
                                previewInferenceResults = liveFrameProcessingResult.previewInferenceResults
                            )
                        }
                    } catch (e: Exception) {
                        emitError(ImagingError.PROCESSING_ERROR)
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
                        workRepository.enqueueSessionUpload(currentSession.localId, currentSessionSiteId)
                        currentSessionCache.clearSession()
                        _events.send(ImagingEvent.NavigateBackToLandingScreen)
                    }
                }

                is ImagingAction.CaptureImage -> {
                    if (!_state.value.isCameraReady) return@launch

                    _state.update { it.copy(isProcessing = true) }
                    val captureResult = cameraRepository.captureImage(action.controller)

                    captureResult.onSuccess { image ->
                        val displayOrientation = _state.value.displayOrientation
                        val bitmap = image.toUprightBitmap(displayOrientation)
                        image.close()

                        val jpegStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpegStream)
                        val jpegByteArray = jpegStream.toByteArray()
                        val jpegBitmap =
                            BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)

                        val capturedFrameProcessingResult =
                            imagingWorkflow.processCapturedFrame(jpegBitmap)

                        capturedFrameProcessingResult.onSuccess { result ->
                            _state.update {
                                it.copy(
                                    currentSpecimenImage = it.currentSpecimenImage.copy(
                                        species = result.species,
                                        sex = result.sex,
                                        abdomenStatus = result.abdomenStatus
                                    ),
                                    currentImageBytes = jpegByteArray,
                                    currentInferenceResult = result.capturedInferenceResult,
                                    previewInferenceResults = emptyList()
                                )
                            }
                        }.onError { error ->
                            emitError(error, SnackbarDuration.Short)
                        }
                    }.onError { error ->
                        if (error == ImagingError.NO_ACTIVE_SESSION) {
                            _events.send(ImagingEvent.NavigateBackToLandingScreen)
                        }
                        emitError(error)
                    }
                    _state.update { it.copy(isProcessing = false) }
                }

                ImagingAction.RetakeImage -> {
                    clearStateFields()
                }

                ImagingAction.SaveImageToSession -> {
                    val jpegBytes = _state.value.currentImageBytes ?: return@launch
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

                    val validationResult = validateSpecimenIdUseCase(specimenId, shouldAutoCorrect = false)
                    if (validationResult is Result.Error) {
                        emitError(validationResult.error)
                        return@launch
                    }

                    val saveResult = cameraRepository.saveImage(jpegBytes, filename, currentSession)

                    saveResult.onSuccess { imageUri ->
                        val specimen = Specimen(id = specimenId, remoteId = null)
                        val specimenImage = SpecimenImage(
                            localId = calculateMd5(jpegBytes),
                            remoteId = null,
                            species = _state.value.currentSpecimenImage.species,
                            sex = _state.value.currentSpecimenImage.sex,
                            abdomenStatus = _state.value.currentSpecimenImage.abdomenStatus,
                            imageUri = imageUri,
                            imageUploadStatus = UploadStatus.NOT_STARTED,
                            metadataUploadStatus = UploadStatus.NOT_STARTED,
                            capturedAt = timestamp,
                            submittedAt = null
                        )

                        val success = transactionHelper.runAsTransaction {
                            val inferenceResult = _state.value.currentInferenceResult

                            val existingSpecimen = specimenRepository.getSpecimenByIdAndSessionId(
                                specimenId, currentSession.localId
                            )
                            val specimenInsertionResult = if (existingSpecimen == null) {
                                specimenRepository.insertSpecimen(specimen, currentSession.localId)
                            } else {
                                Result.Success(Unit)
                            }
                            val specimenImageInsertionResult =
                                specimenImageRepository.insertSpecimenImage(
                                    specimenImage, specimen.id, currentSession.localId
                                )

                            val inferenceResultInsertionResult = inferenceResult?.let {
                                inferenceResultRepository.insertInferenceResult(
                                    inferenceResult, specimenImage.localId
                                )
                            } ?: Result.Success(Unit)

                            specimenInsertionResult.onError { error ->
                                emitError(error)
                            }

                            specimenImageInsertionResult.onError { error ->
                                emitError(error)
                            }

                            inferenceResultInsertionResult.onError { error ->
                                emitError(error)
                            }

                            (specimenInsertionResult !is Result.Error) && (specimenImageInsertionResult !is Result.Error) && (inferenceResultInsertionResult !is Result.Error)
                        }

                        if (success) {
                            clearStateFields()
                        } else {
                            emitError(ImagingError.SAVE_ERROR)
                            cameraRepository.deleteSavedImage(imageUri)
                        }
                    }.onError { error ->
                        emitError(error)
                    }
                }
            }
        }
    }

    private fun clearStateFields() {
        _state.update {
            it.copy(
                currentSpecimen = it.currentSpecimen.copy(
                    id = "",
                    remoteId = null,
                ),
                currentSpecimenImage = it.currentSpecimenImage.copy(
                    localId = "",
                    remoteId = null,
                    species = null,
                    sex = null,
                    abdomenStatus = null,
                    imageUri = Uri.EMPTY,
                    metadataUploadStatus = UploadStatus.NOT_STARTED,
                    imageUploadStatus = UploadStatus.NOT_STARTED,
                    capturedAt = 0L,
                    submittedAt = null
                ),
                currentInferenceResult = null,
                currentImageBytes = null,
                isCameraReady = false,
                previewInferenceResults = emptyList(),
            )
        }
    }


    private fun calculateMd5(imageByteArray: ByteArray): String {
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(imageByteArray)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun loadImagingDetails() {
        viewModelScope.launch {
            orientationListener.enable()
            val session = currentSessionCache.getSession()
            if (session != null) {
                imagingWorkflow = imagingWorkflowFactory.create(session.type)
            } else {
                _events.send(ImagingEvent.NavigateBackToLandingScreen)
                emitError(ImagingError.NO_ACTIVE_SESSION)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        orientationListener.disable()
        imagingWorkflow.close()
    }
}
