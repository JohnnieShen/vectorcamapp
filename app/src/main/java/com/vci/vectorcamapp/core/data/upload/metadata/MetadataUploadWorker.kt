package com.vci.vectorcamapp.core.data.upload.metadata

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.data.dto.device.DeviceDto
import com.vci.vectorcamapp.core.data.dto.inference_result.InferenceResultDto
import com.vci.vectorcamapp.core.data.dto.session.SessionDto
import com.vci.vectorcamapp.core.data.dto.specimen.SpecimenDto
import com.vci.vectorcamapp.core.data.dto.specimen_image.SpecimenImageDto
import com.vci.vectorcamapp.core.data.dto.surveillance_form.SurveillanceFormDto
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.model.enums.UploadStatus
import com.vci.vectorcamapp.core.domain.network.api.DeviceDataSource
import com.vci.vectorcamapp.core.domain.network.api.SessionDataSource
import com.vci.vectorcamapp.core.domain.network.api.SpecimenDataSource
import com.vci.vectorcamapp.core.domain.network.api.SpecimenImageDataSource
import com.vci.vectorcamapp.core.domain.network.api.SurveillanceFormDataSource
import com.vci.vectorcamapp.core.domain.repository.InferenceResultRepository
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenImageRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.repository.SurveillanceFormRepository
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.presentation.util.error.toString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.io.IOException
import java.util.UUID
import androidx.work.ListenableWorker.Result as WorkerResult
import com.vci.vectorcamapp.core.domain.util.Result as DomainResult

@HiltWorker
class MetadataUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val deviceCache: DeviceCache,
    private val transactionHelper: TransactionHelper,
    private val sessionRepository: SessionRepository,
    private val surveillanceFormRepository: SurveillanceFormRepository,
    private val specimenRepository: SpecimenRepository,
    private val specimenImageRepository: SpecimenImageRepository,
    private val inferenceResultRepository: InferenceResultRepository,
    private val deviceDataSource: DeviceDataSource,
    private val sessionDataSource: SessionDataSource,
    private val surveillanceFormDataSource: SurveillanceFormDataSource,
    private val specimenDataSource: SpecimenDataSource,
    private val specimenImageDataSource: SpecimenImageDataSource
) : CoroutineWorker(context, workerParams) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): WorkerResult {
        createNotificationChannel()
        setForeground(showInitialMetadataNotification())

        val localSessionIdString = inputData.getString("session_id")
        val localSiteId = inputData.getInt("site_id", -1)
        if (localSessionIdString == null || localSiteId == -1) {
            return retryOrFailure("Invalid upload request. Check session ID and site ID.")
        }

        val localSessionId = try {
            UUID.fromString(localSessionIdString)
        } catch (e: IllegalArgumentException) {
            return retryOrFailure("Invalid session ID format.")
        }

        val localSession = sessionRepository.getSessionById(localSessionId)
        val localDevice = deviceCache.getDevice()
        val localProgramId = deviceCache.getProgramId()
        if (localSession == null || localDevice == null || localProgramId == null) {
            return retryOrFailure("Device or session not found.")
        }

        try {
            val syncedDevice =
                when (val syncDeviceResult = syncDeviceIfNeeded(localDevice, localProgramId)) {
                    is DomainResult.Success -> syncDeviceResult.data
                    is DomainResult.Error -> return retryOrFailure(
                        syncDeviceResult.error.toString(context)
                    )
                }

            val syncedSession = when (val syncSessionResult =
                syncSessionIfNeeded(localSession, localSiteId, syncedDevice.id)) {
                is DomainResult.Success -> syncSessionResult.data
                is DomainResult.Error -> return retryOrFailure(
                    syncSessionResult.error.toString(context)
                )
            }
            if (syncedSession.remoteId == null) {
                return retryOrFailure("Session not found on the server.")
            }

            val localSurveillanceForm =
                surveillanceFormRepository.getSurveillanceFormBySessionId(syncedSession.localId)
            if (localSurveillanceForm != null) {
                when (val syncSurveillanceFormResult = syncSurveillanceFormIfNeeded(
                    localSurveillanceForm, syncedSession.localId, syncedSession.remoteId
                )) {
                    is DomainResult.Success -> syncSurveillanceFormResult.data
                    is DomainResult.Error -> return retryOrFailure(
                        syncSurveillanceFormResult.error.toString(context)
                    )
                }
            }

            val localSpecimensWithImagesAndInferenceResults =
                specimenRepository.getSpecimenImagesAndInferenceResultsBySession(syncedSession.localId)
            val totalSpecimens = localSpecimensWithImagesAndInferenceResults.size
            localSpecimensWithImagesAndInferenceResults.forEachIndexed { specimenIndex, specimenWithImagesAndInferenceResults ->
                val totalImages =
                    specimenWithImagesAndInferenceResults.specimenImagesAndInferenceResults.size
                val syncedSpecimen = when (val syncSpecimenResult = syncSpecimenIfNeeded(
                    specimenWithImagesAndInferenceResults.specimen,
                    syncedSession.localId,
                    syncedSession.remoteId
                )) {
                    is DomainResult.Success -> syncSpecimenResult.data
                    is DomainResult.Error -> return retryOrFailure(
                        syncSpecimenResult.error.toString(context)
                    )
                }

                specimenWithImagesAndInferenceResults.specimenImagesAndInferenceResults.forEachIndexed { imageIndex, (specimenImage, inferenceResult) ->
                    if (specimenImage.metadataUploadStatus != UploadStatus.COMPLETED) {
                        syncSpecimenImageAndInferenceResultIfNeeded(
                            specimenImage, inferenceResult, syncedSpecimen, syncedSession.localId
                        ).onError { error ->
                            return retryOrFailure(error.toString(context))
                        }
                    }
                    showSpecimenProgressNotification(
                        specimenId = syncedSpecimen.id,
                        currentSpecimenIndex = specimenIndex,
                        totalSpecimens = totalSpecimens,
                        currentImageIndex = imageIndex,
                        totalImagesForSpecimen = totalImages
                    )
                }
            }

            return WorkerResult.success()
        } catch (e: IOException) {
            return retryOrFailure("Lost internet connection while uploading.")
        } catch (e: Exception) {
            return retryOrFailure("An unknown error occurred during upload.")
        } finally {
            if (isStopped) {
                resetInProgressUploads(localSession.localId)
            }
        }
    }

    private suspend fun resetInProgressUploads(sessionId: UUID) {
        val specimensWithImagesAndInferenceResults =
            specimenRepository.getSpecimenImagesAndInferenceResultsBySession(sessionId)
        specimensWithImagesAndInferenceResults.forEach { specimenWithImagesAndInferenceResults ->
            specimenWithImagesAndInferenceResults.specimenImagesAndInferenceResults.forEach { (specimenImage, _) ->
                if (specimenImage.metadataUploadStatus == UploadStatus.IN_PROGRESS) {
                    specimenImageRepository.updateSpecimenImage(
                        specimenImage.copy(metadataUploadStatus = UploadStatus.FAILED),
                        specimenWithImagesAndInferenceResults.specimen.id,
                        sessionId
                    )
                }
            }
        }
    }

    private fun retryOrFailure(message: String): WorkerResult {
        if (runAttemptCount < MAX_RETRIES) {
            showUploadRetryNotification(message)
            return WorkerResult.retry()
        } else {
            showUploadErrorNotification(message)
            return WorkerResult.failure()
        }
    }

    private suspend fun syncDeviceIfNeeded(
        localDevice: Device, localProgramId: Int
    ): DomainResult<Device, NetworkError> {
        return try {
            val localDeviceDto = DeviceDto(
                deviceId = localDevice.id,
                model = localDevice.model,
                registeredAt = localDevice.registeredAt,
                submittedAt = localDevice.submittedAt,
                programId = localProgramId
            )

            val remoteDeviceDto =
                when (val remoteDeviceResult = deviceDataSource.getDeviceById(localDevice.id)) {
                    is DomainResult.Success -> remoteDeviceResult.data
                    is DomainResult.Error -> {
                        when (remoteDeviceResult.error) {
                            NetworkError.NOT_FOUND -> {
                                val registerDeviceResult =
                                    deviceDataSource.registerDevice(localDevice, localProgramId)
                                when (registerDeviceResult) {
                                    is DomainResult.Success -> registerDeviceResult.data.device
                                    is DomainResult.Error -> return DomainResult.Error(
                                        registerDeviceResult.error
                                    )
                                }
                            }

                            else -> return DomainResult.Error(remoteDeviceResult.error)
                        }
                    }
                }

            val remoteDevice = Device(
                id = remoteDeviceDto.deviceId,
                model = remoteDeviceDto.model,
                registeredAt = remoteDeviceDto.registeredAt,
                submittedAt = remoteDeviceDto.submittedAt
            )

            if (remoteDeviceDto != localDeviceDto) {
                deviceCache.saveDevice(remoteDevice, remoteDeviceDto.programId)
            }

            DomainResult.Success(remoteDevice)
        } catch (e: IOException) {
            DomainResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            DomainResult.Error(NetworkError.UNKNOWN_ERROR)
        }
    }

    private suspend fun syncSessionIfNeeded(
        localSession: Session, localSiteId: Int, syncedDeviceId: Int
    ): DomainResult<Session, NetworkError> {
        return try {
            val localSessionDto = SessionDto(
                sessionId = localSession.remoteId,
                frontendId = localSession.localId,
                houseNumber = localSession.houseNumber,
                collectorTitle = localSession.collectorTitle,
                collectorName = localSession.collectorName,
                collectionDate = localSession.collectionDate,
                collectionMethod = localSession.collectionMethod,
                specimenCondition = localSession.specimenCondition,
                createdAt = localSession.createdAt,
                completedAt = localSession.completedAt,
                submittedAt = localSession.submittedAt,
                notes = localSession.notes,
                latitude = localSession.latitude,
                longitude = localSession.longitude,
                type = localSession.type,
                siteId = localSiteId,
                deviceId = syncedDeviceId
            )

            val remoteSessionDto = when (val remoteSessionResult =
                sessionDataSource.getSessionByFrontendId(localSession.localId)) {
                is DomainResult.Success -> remoteSessionResult.data
                is DomainResult.Error -> {
                    when (remoteSessionResult.error) {
                        NetworkError.NOT_FOUND -> {
                            val postSessionResult = sessionDataSource.postSession(
                                localSession, localSiteId, syncedDeviceId
                            )
                            when (postSessionResult) {
                                is DomainResult.Success -> postSessionResult.data.session
                                is DomainResult.Error -> return DomainResult.Error(postSessionResult.error)
                            }
                        }

                        else -> return DomainResult.Error(remoteSessionResult.error)
                    }
                }
            }

            val remoteSession = Session(
                localId = remoteSessionDto.frontendId,
                remoteId = remoteSessionDto.sessionId,
                houseNumber = remoteSessionDto.houseNumber,
                collectorTitle = remoteSessionDto.collectorTitle,
                collectorName = remoteSessionDto.collectorName,
                collectionDate = remoteSessionDto.collectionDate,
                collectionMethod = remoteSessionDto.collectionMethod,
                specimenCondition = remoteSessionDto.specimenCondition,
                createdAt = remoteSessionDto.createdAt,
                completedAt = remoteSessionDto.completedAt,
                submittedAt = remoteSessionDto.submittedAt,
                notes = remoteSessionDto.notes,
                latitude = remoteSessionDto.latitude,
                longitude = remoteSessionDto.longitude,
                type = remoteSessionDto.type
            )

            if (localSessionDto != remoteSessionDto) {
                val updateSessionResult =
                    sessionRepository.upsertSession(remoteSession, remoteSessionDto.siteId)
                if (updateSessionResult is DomainResult.Error) {
                    return DomainResult.Error(NetworkError.CLIENT_ERROR)
                }
            }

            DomainResult.Success(remoteSession)
        } catch (e: IOException) {
            DomainResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            DomainResult.Error(NetworkError.UNKNOWN_ERROR)
        }
    }

    private suspend fun syncSurveillanceFormIfNeeded(
        localSurveillanceForm: SurveillanceForm,
        syncedLocalSessionId: UUID,
        syncedRemoteSessionId: Int
    ): DomainResult<Unit, NetworkError> {
        return try {
            val localSurveillanceFormDto = SurveillanceFormDto(
                sessionId = syncedRemoteSessionId,
                numPeopleSleptInHouse = localSurveillanceForm.numPeopleSleptInHouse,
                wasIrsConducted = localSurveillanceForm.wasIrsConducted,
                monthsSinceIrs = localSurveillanceForm.monthsSinceIrs,
                numLlinsAvailable = localSurveillanceForm.numLlinsAvailable,
                llinType = localSurveillanceForm.llinType,
                llinBrand = localSurveillanceForm.llinBrand,
                numPeopleSleptUnderLlin = localSurveillanceForm.numPeopleSleptUnderLlin,
                submittedAt = localSurveillanceForm.submittedAt
            )

            val remoteSurveillanceFormDto = when (val remoteSurveillanceFormResult =
                surveillanceFormDataSource.getSurveillanceFormBySessionId(syncedRemoteSessionId)) {
                is DomainResult.Success -> remoteSurveillanceFormResult.data
                is DomainResult.Error -> {
                    when (remoteSurveillanceFormResult.error) {
                        NetworkError.NOT_FOUND -> {
                            val postSurveillanceFormResult =
                                surveillanceFormDataSource.postSurveillanceForm(
                                    localSurveillanceForm, syncedRemoteSessionId
                                )
                            when (postSurveillanceFormResult) {
                                is DomainResult.Success -> postSurveillanceFormResult.data.form
                                is DomainResult.Error -> return DomainResult.Error(
                                    postSurveillanceFormResult.error
                                )
                            }
                        }

                        else -> return DomainResult.Error(remoteSurveillanceFormResult.error)
                    }
                }
            }

            val remoteSurveillanceForm = SurveillanceForm(
                numPeopleSleptInHouse = remoteSurveillanceFormDto.numPeopleSleptInHouse,
                wasIrsConducted = remoteSurveillanceFormDto.wasIrsConducted,
                monthsSinceIrs = remoteSurveillanceFormDto.monthsSinceIrs,
                numLlinsAvailable = remoteSurveillanceFormDto.numLlinsAvailable,
                llinType = remoteSurveillanceFormDto.llinType,
                llinBrand = remoteSurveillanceFormDto.llinBrand,
                numPeopleSleptUnderLlin = remoteSurveillanceFormDto.numPeopleSleptUnderLlin,
                submittedAt = remoteSurveillanceFormDto.submittedAt
            )

            if (remoteSurveillanceFormDto != localSurveillanceFormDto) {
                val updateSurveillanceFormResult =
                    surveillanceFormRepository.upsertSurveillanceForm(
                        remoteSurveillanceForm, syncedLocalSessionId
                    )
                if (updateSurveillanceFormResult is DomainResult.Error) {
                    return DomainResult.Error(NetworkError.CLIENT_ERROR)
                }
            }

            DomainResult.Success(Unit)
        } catch (e: IOException) {
            DomainResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            DomainResult.Error(NetworkError.UNKNOWN_ERROR)
        }
    }

    private suspend fun syncSpecimenIfNeeded(
        localSpecimen: Specimen, syncedLocalSessionId: UUID, syncedRemoteSessionId: Int
    ): DomainResult<Specimen, NetworkError> {
        return try {
            val localSpecimenDto = SpecimenDto(
                id = localSpecimen.remoteId,
                specimenId = localSpecimen.id,
                sessionId = syncedRemoteSessionId
            )

            val remoteSpecimenDto =
                when (val remoteSpecimenResult = specimenDataSource.getSpecimenByIdAndSessionId(
                    localSpecimen.id, syncedRemoteSessionId
                )) {
                    is DomainResult.Success -> remoteSpecimenResult.data
                    is DomainResult.Error -> {
                        when (remoteSpecimenResult.error) {
                            NetworkError.NOT_FOUND -> {
                                val postSpecimenResult = specimenDataSource.postSpecimen(
                                    localSpecimen, syncedRemoteSessionId
                                )
                                when (postSpecimenResult) {
                                    is DomainResult.Success -> postSpecimenResult.data.specimen
                                    is DomainResult.Error -> return DomainResult.Error(
                                        postSpecimenResult.error
                                    )
                                }
                            }

                            else -> return DomainResult.Error(remoteSpecimenResult.error)
                        }
                    }
                }

            val remoteSpecimen = Specimen(
                id = remoteSpecimenDto.specimenId, remoteId = remoteSpecimenDto.id
            )

            if (remoteSpecimenDto != localSpecimenDto) {
                val specimenUpdateResult =
                    specimenRepository.updateSpecimen(remoteSpecimen, syncedLocalSessionId)
                if (specimenUpdateResult is DomainResult.Error) {
                    return DomainResult.Error(NetworkError.CLIENT_ERROR)
                }
            }

            DomainResult.Success(remoteSpecimen)
        } catch (e: IOException) {
            DomainResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            DomainResult.Error(NetworkError.UNKNOWN_ERROR)
        }
    }

    private suspend fun syncSpecimenImageAndInferenceResultIfNeeded(
        localSpecimenImage: SpecimenImage,
        localInferenceResult: InferenceResult?,
        syncedSpecimen: Specimen,
        syncedLocalSessionId: UUID
    ): DomainResult<Unit, NetworkError> {
        return try {
            specimenImageRepository.updateSpecimenImage(
                localSpecimenImage.copy(metadataUploadStatus = UploadStatus.IN_PROGRESS),
                syncedSpecimen.id,
                syncedLocalSessionId
            )

            val localSpecimenImageDto = SpecimenImageDto(
                id = localSpecimenImage.remoteId,
                filemd5 = localSpecimenImage.localId,
                species = localSpecimenImage.species,
                sex = localSpecimenImage.sex,
                abdomenStatus = localSpecimenImage.abdomenStatus,
                capturedAt = localSpecimenImage.capturedAt,
                submittedAt = localSpecimenImage.submittedAt,
                inferenceResult = localInferenceResult?.let {
                    InferenceResultDto(
                        bboxTopLeftX = it.bboxTopLeftX,
                        bboxTopLeftY = it.bboxTopLeftY,
                        bboxWidth = it.bboxWidth,
                        bboxHeight = it.bboxHeight,
                        bboxConfidence = it.bboxConfidence,
                        bboxClassId = it.bboxClassId,
                        speciesLogits = it.speciesLogits,
                        sexLogits = it.sexLogits,
                        abdomenStatusLogits = it.abdomenStatusLogits
                    )
                })

            if (syncedSpecimen.remoteId == null) {
                return DomainResult.Error(NetworkError.CLIENT_ERROR)
            }

            val remoteSpecimenImageDto = when (val remoteSpecimenImageResult =
                specimenImageDataSource.getSpecimenImageMetadata(
                    localSpecimenImage.localId, syncedSpecimen.remoteId
                )) {
                is DomainResult.Success -> remoteSpecimenImageResult.data
                is DomainResult.Error -> {
                    when (remoteSpecimenImageResult.error) {
                        NetworkError.NOT_FOUND -> {
                            val postSpecimenImageResult =
                                specimenImageDataSource.postSpecimenImageMetadata(
                                    localSpecimenImage,
                                    localInferenceResult,
                                    syncedSpecimen.remoteId
                                )
                            when (postSpecimenImageResult) {
                                is DomainResult.Success -> {
                                    postSpecimenImageResult.data.image
                                }

                                is DomainResult.Error -> {
                                    specimenImageRepository.updateSpecimenImage(
                                        localSpecimenImage.copy(
                                            metadataUploadStatus = UploadStatus.FAILED
                                        ), syncedSpecimen.id, syncedLocalSessionId
                                    )
                                    return DomainResult.Error(
                                        postSpecimenImageResult.error
                                    )
                                }
                            }
                        }

                        else -> {
                            specimenImageRepository.updateSpecimenImage(
                                localSpecimenImage.copy(
                                    metadataUploadStatus = UploadStatus.FAILED
                                ), syncedSpecimen.id, syncedLocalSessionId
                            )
                            return DomainResult.Error(remoteSpecimenImageResult.error)
                        }
                    }
                }
            }

            val remoteSpecimenImage = SpecimenImage(
                localId = remoteSpecimenImageDto.filemd5,
                remoteId = remoteSpecimenImageDto.id,
                species = remoteSpecimenImageDto.species,
                sex = remoteSpecimenImageDto.sex,
                abdomenStatus = remoteSpecimenImageDto.abdomenStatus,
                imageUri = localSpecimenImage.imageUri,
                metadataUploadStatus = localSpecimenImage.metadataUploadStatus,
                imageUploadStatus = localSpecimenImage.imageUploadStatus,
                capturedAt = remoteSpecimenImageDto.capturedAt,
                submittedAt = remoteSpecimenImageDto.submittedAt
            )

            val remoteInferenceResult = remoteSpecimenImageDto.inferenceResult?.let {
                InferenceResult(
                    bboxTopLeftX = it.bboxTopLeftX,
                    bboxTopLeftY = it.bboxTopLeftY,
                    bboxWidth = it.bboxWidth,
                    bboxHeight = it.bboxHeight,
                    bboxConfidence = it.bboxConfidence,
                    bboxClassId = it.bboxClassId,
                    speciesLogits = it.speciesLogits,
                    sexLogits = it.sexLogits,
                    abdomenStatusLogits = it.abdomenStatusLogits
                )
            }

            if (remoteSpecimenImageDto != localSpecimenImageDto) {
                val transactionResult = transactionHelper.runAsTransaction {
                    specimenImageRepository.updateSpecimenImage(
                        remoteSpecimenImage, syncedSpecimen.id, syncedLocalSessionId
                    ).onError {
                        return@runAsTransaction DomainResult.Error(NetworkError.CLIENT_ERROR)
                    }

                    remoteInferenceResult?.let { remoteInferenceResult ->
                        inferenceResultRepository.updateInferenceResult(
                            remoteInferenceResult, remoteSpecimenImage.localId
                        ).onError {
                            return@runAsTransaction DomainResult.Error(NetworkError.CLIENT_ERROR)
                        }
                    }

                    DomainResult.Success(Unit)
                }

                if (transactionResult is DomainResult.Error) {
                    return DomainResult.Error(transactionResult.error)
                }
            }

            specimenImageRepository.updateSpecimenImage(
                remoteSpecimenImage.copy(metadataUploadStatus = UploadStatus.COMPLETED),
                syncedSpecimen.id,
                syncedLocalSessionId
            )
            DomainResult.Success(Unit)
        } catch (e: IOException) {
            specimenImageRepository.updateSpecimenImage(
                localSpecimenImage.copy(metadataUploadStatus = UploadStatus.FAILED),
                syncedSpecimen.id,
                syncedLocalSessionId
            )
            DomainResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            specimenImageRepository.updateSpecimenImage(
                localSpecimenImage.copy(metadataUploadStatus = UploadStatus.FAILED),
                syncedSpecimen.id,
                syncedLocalSessionId
            )
            DomainResult.Error(NetworkError.UNKNOWN_ERROR)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun showInitialMetadataNotification(): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Registering device and session...")
            .setSmallIcon(R.drawable.ic_cloud_upload).setOngoing(true).build()

        return ForegroundInfo(
            NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun showSpecimenProgressNotification(
        specimenId: String,
        currentSpecimenIndex: Int,
        totalSpecimens: Int,
        currentImageIndex: Int,
        totalImagesForSpecimen: Int
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading metadata for specimen $specimenId")
            .setContentText("Specimen ${currentSpecimenIndex + 1} of $totalSpecimens").setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    """
                        Specimen ${currentSpecimenIndex + 1} of $totalSpecimens
                        Image ${currentImageIndex + 1} of $totalImagesForSpecimen
                    """.trimIndent()
                )
            ).setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(totalSpecimens, currentSpecimenIndex + 1, false).setOngoing(true).build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showUploadRetryNotification(message: String) {
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Retrying upload...")
                .setContentText(message).setSmallIcon(R.drawable.ic_error).build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showUploadErrorNotification(message: String) {
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Upload failed")
                .setContentText(message).setSmallIcon(R.drawable.ic_error).build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val MAX_RETRIES = 5
        const val CHANNEL_ID = "metadata_upload_channel"
        const val CHANNEL_NAME = "Metadata Upload Channel"
        const val NOTIFICATION_ID = 1002
    }
}
