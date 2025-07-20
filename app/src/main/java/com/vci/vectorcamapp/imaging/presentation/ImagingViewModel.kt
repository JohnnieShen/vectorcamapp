package com.vci.vectorcamapp.imaging.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.OrientationEventListener
import androidx.compose.material3.SnackbarDuration
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
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenAndInferenceResult
import com.vci.vectorcamapp.core.domain.repository.InferenceResultRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.core.presentation.CoreViewModel
import com.vci.vectorcamapp.imaging.domain.enums.AbdomenStatusLabel
import com.vci.vectorcamapp.imaging.domain.enums.SexLabel
import com.vci.vectorcamapp.imaging.domain.enums.SpeciesLabel
import com.vci.vectorcamapp.imaging.domain.repository.CameraRepository
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import com.vci.vectorcamapp.imaging.presentation.extensions.toUprightBitmap
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ImagingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val currentSessionCache: CurrentSessionCache,
    private val sessionRepository: SessionRepository,
    private val specimenRepository: SpecimenRepository,
    private val inferenceResultRepository: InferenceResultRepository,
    private val cameraRepository: CameraRepository,
    private val inferenceRepository: InferenceRepository
) : CoreViewModel() {

    @Inject
    lateinit var transactionHelper: TransactionHelper

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _specimensAndInferenceResults: Flow<List<SpecimenAndInferenceResult>> = flow {
        emit(currentSessionCache.getSession())
    }.flatMapLatest { session ->
        if (session == null) {
            flowOf(emptyList())
        } else {
            specimenRepository.observeSpecimensAndInferenceResultsBySession(session.localId)
                .map { relations ->
                    relations.map { relation ->
                        SpecimenAndInferenceResult(
                            specimen = relation.specimen, inferenceResult = relation.inferenceResult
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
        _specimensAndInferenceResults, _state
    ) { specimensAndInferenceResults, state ->
        state.copy(capturedSpecimensAndInferenceResults = specimensAndInferenceResults)
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
                emitError(ImagingError.NO_ACTIVE_SESSION)
            }
        }
    }

    fun onAction(action: ImagingAction) {
        viewModelScope.launch {
            when (action) {
                is ImagingAction.ManualFocusAt -> {
                    _state.update { it.copy(manualFocusPoint = action.offset) }
                }

                is ImagingAction.CancelManualFocus -> {
                    _state.update { it.copy(manualFocusPoint = null) }
                }

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
                        val previewInferenceResults = inferenceRepository.detectSpecimen(bitmap)

                        _state.update {
                            it.copy(
                                currentSpecimen = it.currentSpecimen.copy(id = specimenId),
                                previewInferenceResults = previewInferenceResults
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

                        // Avoid issuing error if preview bounding boxes are not yet ready
                        val captureInferenceResults = inferenceRepository.detectSpecimen(jpegBitmap)

                        when (captureInferenceResults.size) {
                            0 -> {
                                emitError(ImagingError.NO_SPECIMEN_FOUND, SnackbarDuration.Short)
                            }

                            1 -> {
                                val captureInferenceResult = captureInferenceResults.first()
                                val topLeftXFloat = captureInferenceResult.bboxTopLeftX * bitmap.width
                                val topLeftYFloat = captureInferenceResult.bboxTopLeftY * bitmap.height
                                val widthFloat = captureInferenceResult.bboxWidth * bitmap.width
                                val heightFloat = captureInferenceResult.bboxHeight * bitmap.height

                                val topLeftXAbsolute = topLeftXFloat.toInt()
                                val topLeftYAbsolute = topLeftYFloat.toInt()
                                val widthAbsolute = (widthFloat + (topLeftXFloat - topLeftXAbsolute)).toInt()
                                val heightAbsolute = (heightFloat + (topLeftYFloat - topLeftYAbsolute)).toInt()

                                // Clamp the crop rectangle to stay within bitmap bounds
                                val clampedTopLeftX = topLeftXAbsolute.coerceIn(0, jpegBitmap.width - 1)
                                val clampedTopLeftY = topLeftYAbsolute.coerceIn(0, jpegBitmap.height - 1)
                                val clampedWidth = widthAbsolute.coerceIn(1, jpegBitmap.width - clampedTopLeftX)
                                val clampedHeight = heightAbsolute.coerceIn(1, jpegBitmap.height - clampedTopLeftY)

                                if (clampedWidth > 0 && clampedHeight > 0) {
                                    val croppedBitmap = Bitmap.createBitmap(
                                        jpegBitmap,
                                        clampedTopLeftX,
                                        clampedTopLeftY,
                                        clampedWidth,
                                        clampedHeight
                                    )
                                    var (speciesLogits, sexLogits, abdomenStatusLogits) = inferenceRepository.classifySpecimen(croppedBitmap)

                                    val speciesIndex = speciesLogits?.let { logits -> logits.indexOf(logits.max()) }
                                    var sexIndex = sexLogits?.let { logits -> logits.indexOf(logits.max()) }
                                    var abdomenStatusIndex = abdomenStatusLogits?.let { logits -> logits.indexOf(logits.max()) }

                                    if (speciesLogits == null || speciesIndex == SpeciesLabel.NON_MOSQUITO.ordinal) {
                                        sexLogits = null
                                        sexIndex = null
                                    }
                                    if (sexLogits == null || sexIndex == SexLabel.MALE.ordinal) {
                                        abdomenStatusLogits = null
                                        abdomenStatusIndex = null
                                    }

                                    _state.update {
                                        it.copy(
                                            currentSpecimen = it.currentSpecimen.copy(
                                                species = speciesIndex?.let { index -> SpeciesLabel.entries[index].label },
                                                sex = sexIndex?.let { index -> SexLabel.entries[index].label },
                                                abdomenStatus = abdomenStatusIndex?.let { index -> AbdomenStatusLabel.entries[index].label },
                                            ),
                                            currentImageBytes = jpegByteArray,
                                            currentInferenceResult = it.currentInferenceResult.copy(
                                                bboxTopLeftX = captureInferenceResult.bboxTopLeftX,
                                                bboxTopLeftY = captureInferenceResult.bboxTopLeftY,
                                                bboxWidth = captureInferenceResult.bboxWidth,
                                                bboxHeight = captureInferenceResult.bboxHeight,
                                                bboxConfidence = captureInferenceResult.bboxConfidence,
                                                bboxClassId = captureInferenceResult.bboxClassId,
                                                speciesLogits = speciesLogits,
                                                sexLogits = sexLogits,
                                                abdomenStatusLogits = abdomenStatusLogits
                                            ),
                                            previewInferenceResults = emptyList()
                                        )
                                    }
                                } else {
                                    emitError(ImagingError.PROCESSING_ERROR, SnackbarDuration.Short)
                                }
                            }

                            else -> {
                                emitError(
                                    ImagingError.MULTIPLE_SPECIMENS_FOUND, SnackbarDuration.Short
                                )
                            }
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
                    clearCurrentSpecimenStateFields()
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

                    val saveResult =
                        cameraRepository.saveImage(jpegBytes, filename, currentSession)

                    saveResult.onSuccess { imageUri ->
                        val specimen = Specimen(
                            id = specimenId,
                            species = _state.value.currentSpecimen.species,
                            sex = _state.value.currentSpecimen.sex,
                            abdomenStatus = _state.value.currentSpecimen.abdomenStatus,
                            imageUri = imageUri,
                            imageUploadStatus = UploadStatus.NOT_STARTED,
                            metadataUploadStatus = UploadStatus.NOT_STARTED,
                            capturedAt = timestamp,
                            submittedAt = null
                        )

                        val success = transactionHelper.runAsTransaction {
                            val inferenceResult = _state.value.currentInferenceResult

                            val specimenInsertionResult =
                                specimenRepository.insertSpecimen(specimen, currentSession.localId)
                            val inferenceResultInsertionResult =
                                inferenceResultRepository.insertInferenceResult(inferenceResult, specimen.id)

                            specimenInsertionResult.onError { error ->
                                emitError(error)
                            }

                            inferenceResultInsertionResult.onError { error ->
                                emitError(error)
                            }

                            (specimenInsertionResult !is Result.Error) && (inferenceResultInsertionResult !is Result.Error)
                        }

                        if (success) {
                            clearCurrentSpecimenStateFields()
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

    private fun clearCurrentSpecimenStateFields() {
        _state.update {
            it.copy(
                currentSpecimen = it.currentSpecimen.copy(
                    id = "",
                    species = null,
                    sex = null,
                    abdomenStatus = null,
                    imageUri = Uri.EMPTY,
                    metadataUploadStatus = UploadStatus.NOT_STARTED,
                    imageUploadStatus = UploadStatus.NOT_STARTED,
                    capturedAt = 0L,
                    submittedAt = null
                ),
                currentInferenceResult = InferenceResult(
                    bboxTopLeftX = 0f,
                    bboxTopLeftY = 0f,
                    bboxWidth = 0f,
                    bboxHeight = 0f,
                    bboxConfidence = 0f,
                    bboxClassId = 0,
                    speciesLogits = null,
                    sexLogits = null,
                    abdomenStatusLogits = null
                ),
                currentImageBytes = null,
                previewInferenceResults = emptyList(),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        orientationListener.disable()
        inferenceRepository.closeResources()
    }
}
