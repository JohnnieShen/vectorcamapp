package com.vci.vectorcamapp.imaging.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenAndBoundingBox
import com.vci.vectorcamapp.core.domain.repository.BoundingBoxRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.core.presentation.CoreViewModel
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
    private val boundingBoxRepository: BoundingBoxRepository,
    private val cameraRepository: CameraRepository,
    private val inferenceRepository: InferenceRepository
) : CoreViewModel() {

    companion object {
        private const val SPECIMEN_IMAGE_ENDPOINT_TEMPLATE = "https://api.vectorcam.org/specimens/%s/images/tus"
        private const val UPLOAD_WORK_CHAIN_NAME = "vectorcam_session_upload_chain"
    }

    @Inject
    lateinit var transactionHelper: TransactionHelper

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _specimensAndBoundingBoxes: Flow<List<SpecimenAndBoundingBox>> = flow {
        emit(currentSessionCache.getSession())
    }.flatMapLatest { session ->
        if (session == null) {
            flowOf(emptyList())
        } else {
            specimenRepository.observeSpecimensAndBoundingBoxesBySession(session.localId)
                .map { relations ->
                    relations.map { relation ->
                        SpecimenAndBoundingBox(
                            specimen = relation.specimen, boundingBox = relation.boundingBox
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
        _specimensAndBoundingBoxes, _state
    ) { specimens, state ->
        state.copy(capturedSpecimensAndBoundingBoxes = specimens)
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
                        val boundingBoxes = inferenceRepository.detectSpecimen(bitmap)

                        _state.update {
                            it.copy(
                                currentSpecimen = it.currentSpecimen.copy(id = specimenId),
                                previewBoundingBoxes = boundingBoxes
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
                        val workManager = WorkManager.getInstance(context)
                        val uploadConstraints =
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
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

                        val sessionWithSpecimens =
                            sessionRepository.getSessionWithSpecimensById(currentSession.localId)

                        val imageUploadRequests = sessionWithSpecimens?.specimens?.map { specimen ->
                            val endpoint = SPECIMEN_IMAGE_ENDPOINT_TEMPLATE.format(specimen.id)
                            OneTimeWorkRequestBuilder<ImageUploadWorker>().setInputData(
                                workDataOf(
                                    ImageUploadWorker.KEY_URI to specimen.imageUri.toString(),
                                    ImageUploadWorker.KEY_ENDPOINT to endpoint,
                                    ImageUploadWorker.KEY_SPECIMEN_ID to specimen.id,
                                    ImageUploadWorker.KEY_SESSION_ID to currentSession.localId.toString()
                                )
                            )
                                .setConstraints(uploadConstraints)
                                .setBackoffCriteria(
                                    BackoffPolicy.LINEAR,
                                    WorkRequest.MIN_BACKOFF_MILLIS,
                                    TimeUnit.MILLISECONDS
                                )
                                .build()
                        }

                        if (!imageUploadRequests.isNullOrEmpty()) {
                            workManager.beginUniqueWork(
                                UPLOAD_WORK_CHAIN_NAME,
                                ExistingWorkPolicy.APPEND_OR_REPLACE,
                                metadataUploadRequest
                            ).then(imageUploadRequests).enqueue()
                        } else {
                            workManager.enqueueUniqueWork(
                                UPLOAD_WORK_CHAIN_NAME,
                                ExistingWorkPolicy.APPEND_OR_REPLACE,
                                metadataUploadRequest
                            )
                        }

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

                        val jpegStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpegStream)
                        val jpegByteArray = jpegStream.toByteArray()
                        val jpegBitmap =
                            BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)

                        // Avoid issuing error if preview bounding boxes are not yet ready
                        val boundingBoxesList = inferenceRepository.detectSpecimen(jpegBitmap)

                        when (boundingBoxesList.size) {
                            0 -> {
                                emitError(ImagingError.NO_SPECIMEN_FOUND, SnackbarDuration.Short)
                            }

                            1 -> {
                                val boundingBox = boundingBoxesList[0]
                                val topLeftXFloat = boundingBox.topLeftX * bitmap.width
                                val topLeftYFloat = boundingBox.topLeftY * bitmap.height
                                val widthFloat = boundingBox.width * bitmap.width
                                val heightFloat = boundingBox.height * bitmap.height

                                val topLeftXAbsolute = topLeftXFloat.toInt()
                                val topLeftYAbsolute = topLeftYFloat.toInt()
                                val widthAbsolute = (widthFloat + (topLeftXFloat - topLeftXAbsolute)).toInt()
                                val heightAbsolute = (heightFloat + (topLeftYFloat - topLeftYAbsolute)).toInt()

                                val croppedBitmap = Bitmap.createBitmap(
                                    jpegBitmap,
                                    topLeftXAbsolute,
                                    topLeftYAbsolute,
                                    widthAbsolute,
                                    heightAbsolute
                                )
                                val (species, sex, abdomenStatus) = inferenceRepository.classifySpecimen(
                                    croppedBitmap
                                )

                                _state.update {
                                    it.copy(
                                        currentSpecimen = it.currentSpecimen.copy(
                                            species = species?.label,
                                            sex = sex?.label,
                                            abdomenStatus = abdomenStatus?.label,
                                        ),
                                        currentImageBytes = jpegByteArray,
                                        captureBoundingBox = boundingBox,
                                        previewBoundingBoxes = emptyList()
                                    )
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
                            val boundingBox =
                                _state.value.captureBoundingBox ?: return@runAsTransaction false

                            val specimenResult =
                                specimenRepository.insertSpecimen(specimen, currentSession.localId)
                            val boundingBoxResult =
                                boundingBoxRepository.insertBoundingBox(boundingBox, specimen.id)

                            specimenResult.onError { error ->
                                emitError(error)
                            }

                            boundingBoxResult.onError { error ->
                                emitError(error)
                            }

                            (specimenResult !is Result.Error) && (boundingBoxResult !is Result.Error)
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
                    id = "", species = null, sex = null, abdomenStatus = null
                ),
                currentImageBytes = null,
                captureBoundingBox = null,
                previewBoundingBoxes = emptyList(),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        orientationListener.disable()
        inferenceRepository.closeResources()
    }
}
