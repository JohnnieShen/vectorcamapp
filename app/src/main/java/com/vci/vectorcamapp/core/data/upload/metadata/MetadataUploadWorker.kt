package com.vci.vectorcamapp.core.data.upload.metadata

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.network.api.DeviceDataSource
import com.vci.vectorcamapp.core.domain.network.api.SessionDataSource
import com.vci.vectorcamapp.core.domain.network.api.SpecimenDataSource
import com.vci.vectorcamapp.core.domain.network.api.SurveillanceFormDataSource
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.repository.SurveillanceFormRepository
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class MetadataUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val deviceCache: DeviceCache,
    private val sessionRepository: SessionRepository,
    private val surveillanceFormRepository: SurveillanceFormRepository,
    private val specimenRepository: SpecimenRepository,
    private val deviceDataSource: DeviceDataSource,
    private val sessionDataSource: SessionDataSource,
    private val surveillanceFormDataSource: SurveillanceFormDataSource,
    private val specimenDataSource: SpecimenDataSource
) : CoroutineWorker(context, workerParams) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // TODO: Update once Inference Result data points become available
    override suspend fun doWork(): Result {
        createNotificationChannel()

        setForeground(showInitialMetadataNotification())

        val session = inputData.getString("session_id")?.let { UUID.fromString(it) }
            ?.let { sessionRepository.getSessionById(it) }
        val siteId = inputData.getInt("site_id", -1)

        var device = deviceCache.getDevice()
        val programId = deviceCache.getProgramId()

        if (session == null || siteId == -1 || device == null || programId == null) return Result.failure()

        try {
            if (device.submittedAt == null) {
                deviceDataSource.registerDevice(device, programId)
                    .onSuccess { registerDeviceResponseDto ->
                        val deviceDto = registerDeviceResponseDto.device
                        deviceCache.saveDevice(
                            Device(
                                id = deviceDto.deviceId,
                                model = deviceDto.model,
                                registeredAt = deviceDto.registeredAt,
                                submittedAt = deviceDto.submittedAt
                            ), deviceDto.programId
                        )
                    }.onError {
                        Log.e("UploadWorker", "Error during device registration: $it")
                        showUploadErrorNotification("Error during device registration: $it")
                        return Result.failure()
                    }
            }

            device = deviceCache.getDevice()
            Log.d("UploadWorker", "Device: $device")
            if (device == null) return Result.failure()

            if (session.submittedAt == null) {
                sessionDataSource.postSession(session, siteId, device.id)
                    .onSuccess { postSessionResponseDto ->
                        val sessionDto = postSessionResponseDto.session
                        sessionRepository.upsertSession(
                            Session(
                                localId = sessionDto.frontendId,
                                remoteId = sessionDto.sessionId,
                                houseNumber = sessionDto.houseNumber,
                                collectorTitle = sessionDto.collectorTitle,
                                collectorName = sessionDto.collectorName,
                                collectionDate = sessionDto.collectionDate,
                                collectionMethod = sessionDto.collectionMethod,
                                specimenCondition = sessionDto.specimenCondition,
                                createdAt = sessionDto.createdAt,
                                completedAt = sessionDto.completedAt,
                                submittedAt = sessionDto.submittedAt,
                                notes = sessionDto.notes
                            ), sessionDto.siteId
                        ).onError {
                            Log.e("UploadWorker", "Error during saving session locally: $it")
                            showUploadErrorNotification("Error during saving session locally: $it")
                            return Result.failure()
                        }
                    }.onError {
                        Log.e("UploadWorker", "Error during session upload: $it")
                        showUploadErrorNotification("Error during session upload: $it")
                        return Result.failure()
                    }
            }

            val submittedSession = sessionRepository.getSessionById(session.localId)
            if (submittedSession?.remoteId == null) {
                Log.e("UploadWorker", "Submitted session not found locally")
                showUploadErrorNotification("Submitted session not found locally")
                return Result.failure()
            }

            val surveillanceForm =
                surveillanceFormRepository.getSurveillanceFormBySessionId(submittedSession.localId)
            if (surveillanceForm != null) {
                if (surveillanceForm.submittedAt == null) {
                    surveillanceFormDataSource.postSurveillanceForm(
                        surveillanceForm, submittedSession.remoteId
                    ).onSuccess { postSurveillanceFormResponseDto ->
                        val surveillanceFormDto = postSurveillanceFormResponseDto.form
                        surveillanceFormRepository.upsertSurveillanceForm(
                            SurveillanceForm(
                                numPeopleSleptInHouse = surveillanceFormDto.numPeopleSleptInHouse,
                                wasIrsConducted = surveillanceFormDto.wasIrsConducted,
                                monthsSinceIrs = surveillanceFormDto.monthsSinceIrs,
                                numLlinsAvailable = surveillanceFormDto.numLlinsAvailable,
                                llinType = surveillanceFormDto.llinType,
                                llinBrand = surveillanceFormDto.llinBrand,
                                numPeopleSleptUnderLlin = surveillanceFormDto.numPeopleSleptUnderLlin,
                                submittedAt = surveillanceFormDto.submittedAt
                            ), submittedSession.localId
                        ).onError {
                            Log.e(
                                "UploadWorker", "Error during saving surveillance form locally: $it"
                            )
                            showUploadErrorNotification("Error during saving surveillance form locally: $it")
                            return Result.failure()
                        }
                    }.onError {
                        Log.e("UploadWorker", "Error during surveillance form upload: $it")
                        showUploadErrorNotification("Error during surveillance form upload: $it")
                        return Result.failure()
                    }
                }
            }

            val specimensAndBoundingBoxes =
                specimenRepository.getSpecimensAndBoundingBoxesBySession(submittedSession.localId)

            specimensAndBoundingBoxes.forEachIndexed { index, (specimen, boundingBox) ->
                if (specimen.submittedAt == null) {
                    specimenDataSource.postSpecimen(
                        specimen, boundingBox, submittedSession.remoteId
                    ).onSuccess { postSpecimenResponseDto ->
                        val specimenDto = postSpecimenResponseDto.specimen
                        specimenRepository.updateSpecimen(
                            Specimen(
                                id = specimenDto.specimenId,
                                species = specimenDto.species,
                                sex = specimenDto.sex,
                                abdomenStatus = specimenDto.abdomenStatus,
                                imageUri = specimen.imageUri,
                                capturedAt = specimenDto.capturedAt,
                                submittedAt = specimenDto.submittedAt
                            ), submittedSession.localId
                        ).onSuccess {
                            Log.d("UploadWorker", "Specimen: $specimen, BoundingBox: $boundingBox")
                            showSpecimenUploadProgress(index + 1, specimensAndBoundingBoxes.size)
                        }.onError {
                            Log.e("UploadWorker", "Error during saving specimen locally: $it")
                            showUploadErrorNotification("Error during saving specimen locally: $it")
                            return Result.failure()
                        }
                    }.onError {
                        Log.e("UploadWorker", "Error during specimen upload: $it")
                        showUploadErrorNotification("Error during specimen upload: $it")
                        return Result.failure()
                    }
                }
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("UploadWorker", "Error during upload: ${e.message}")
            e.message?.let { showUploadErrorNotification(it) }
            return Result.failure()
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
            .setContentTitle("Registering device and session...").setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true).build()

        return ForegroundInfo(
            NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun showSpecimenUploadProgress(current: Int, total: Int) {
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Uploading specimens")
                .setContentText("Uploading $current of $total specimens")
                .setSmallIcon(R.drawable.ic_upload).setProgress(total, current, false)
                .setOngoing(true).build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showUploadErrorNotification(message: String) {
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Upload failed")
                .setContentText(message).setSmallIcon(R.drawable.ic_error).build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "metadata_upload_channel"
        const val CHANNEL_NAME = "Metadata Upload Channel"
        const val NOTIFICATION_ID = 1002
    }
}
