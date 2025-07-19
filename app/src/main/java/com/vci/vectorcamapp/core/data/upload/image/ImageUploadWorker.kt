package com.vci.vectorcamapp.core.data.upload.image

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.network.api.ImageUploadDataSource
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.successOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.tus.java.client.TusClient
import io.tus.java.client.TusUpload
import io.tus.java.client.TusUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID
import androidx.work.ListenableWorker.Result as WorkerResult
import com.vci.vectorcamapp.core.domain.util.Result as DomainResult

@HiltWorker
class ImageUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val specimenRepository: SpecimenRepository,
    private val sessionRepository: SessionRepository,
    private val imageUploadDataSource: ImageUploadDataSource,
    private val tusClient: TusClient
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SESSION_ID = "session_id"

        const val KEY_PROGRESS_UPLOADED = "progress_uploaded"
        const val KEY_PROGRESS_TOTAL = "progress_total"

        private const val INITIAL_CHUNK_SIZE_BYTES = 64 * 1024
        private const val MIN_CHUNK_SIZE_BYTES = 16 * 1024
        private const val MAX_CHUNK_SIZE_BYTES = 1024 * 1024
        private const val SUCCESS_STREAK_FOR_INCREASE = 5
        private const val SUCCESS_CHUNK_SIZE_MULTIPLIER = 2
        private const val FAILURE_CHUNK_SIZE_DIVIDER = 2

        private const val NETWORK_TIMEOUT_MS = 30_000L
        private const val MAX_RETRIES = 5

        private const val CHANNEL_ID = "image_upload_channel"
        private const val CHANNEL_NAME = "Image Upload Channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): WorkerResult {
        val sessionId = getSessionId() ?: return WorkerResult.failure(
            workDataOf("error" to "Invalid Session ID provided.")
        )

        val sessionWithSpecimens = sessionRepository.getSessionWithSpecimensById(sessionId)
        if (sessionWithSpecimens == null) {
            Log.e("ImageUploadWorker", "Session $sessionId not found in the database.")
            return WorkerResult.failure(workDataOf("error" to "Session not found."))
        }

        val specimensToUpload = sessionWithSpecimens.specimens.filter {
            it.imageUploadStatus != UploadStatus.COMPLETED
        }

        if (specimensToUpload.isEmpty()) {
            Log.d("ImageUploadWorker", "No images to upload for session $sessionId.")
            return WorkerResult.success()
        }

        createNotificationChannel()
        val shortSessionId = sessionId.toString().substring(0, 8)
        setForeground(showInitialSessionNotification(shortSessionId, specimensToUpload.size))

        var successfulUploads = 0

        specimensToUpload.forEachIndexed { index, specimen ->
            val currentIndex = index + 1

            var imageUploadSuccess = false
            var attempt = 0
            var permanentErrorOccurred = false

            updateStatus(specimen.id, sessionId, UploadStatus.IN_PROGRESS)

            while (attempt < MAX_RETRIES && !imageUploadSuccess && !permanentErrorOccurred) {
                attempt++
                var temporaryFile: File? = null

                try {
                    Log.d("ImageUploadWorker", "Uploading specimen ${specimen.id} (Attempt $attempt/$MAX_RETRIES)")
                    updateSessionProgressNotification(
                        shortSessionId,
                        currentIndex,
                        specimensToUpload.size,
                        "Preparing (Attempt $attempt)..."
                    )

                    val (file, contentType) = prepareFile(context.contentResolver, specimen.imageUri)
                    temporaryFile = file

                    when (val result = performUpload(file, contentType, sessionId, currentIndex, specimensToUpload.size, shortSessionId, specimen.id)) {
                        is DomainResult.Success -> {
                            successfulUploads++
                            imageUploadSuccess = true
                            updateStatus(specimen.id, sessionId, UploadStatus.COMPLETED)
                            Log.d("ImageUploadWorker", "Success for specimen ${specimen.id} on attempt $attempt")
                        }
                        is DomainResult.Error -> {
                            val error = result.error
                            Log.w("ImageUploadWorker", "Failed attempt $attempt for specimen ${specimen.id} with error: $error")
                            if (error != NetworkError.REQUEST_TIMEOUT && error != NetworkError.NO_INTERNET) {
                                permanentErrorOccurred = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImageUploadWorker", "Exception on attempt $attempt for specimen ${specimen.id}. This is a permanent failure for this image.", e)
                    permanentErrorOccurred = true
                } finally {
                    temporaryFile?.delete()
                }
            }

            if (!imageUploadSuccess) {
                Log.e("ImageUploadWorker", "All attempts failed or a permanent error occurred for specimen ${specimen.id}. Moving to next image.")
                updateStatus(specimen.id, sessionId, UploadStatus.FAILED)
            }
        }

        showFinalStatusNotification(shortSessionId, successfulUploads, specimensToUpload.size)
        return WorkerResult.success()
    }

    private suspend fun performUpload(file: File, contentType: String?, sessionId: UUID, currentIndex: Int, totalImages: Int, shortSessionId: String, uploadSpecimenId: String): DomainResult<String, NetworkError> {
        val md5 = calculateMD5(file)
        val uniqueFingerprint = "$uploadSpecimenId-$md5"
        val tusPath = "specimens/$uploadSpecimenId/images/tus"

        tusClient.uploadCreationURL = URL(constructUrl(tusPath))
        val upload = createTusUpload(file, uniqueFingerprint, contentType, md5)

        Log.d("ImageUploadWorker", "Start/resume ${file.name} (fp=$uniqueFingerprint,md5=$md5)")

        val uploaderResult = try {
            DomainResult.Success(tusClient.resumeOrCreateUpload(upload))
        } catch (e: io.tus.java.client.ProtocolException) {
            if (e.causingConnection?.responseCode == HttpURLConnection.HTTP_CONFLICT) {
                Log.w("ImageUploadWorker", "resumeOrCreateUpload failed with 409 Conflict. Verifying by MD5.", e)
                return when (val existingUrlResult = imageUploadDataSource.imageExists(uploadSpecimenId, md5)) {
                    is DomainResult.Success -> DomainResult.Success(existingUrlResult.data.toString())
                    is DomainResult.Error -> {
                        Log.e("ImageUploadWorker", "MD5 check failed despite 409. Upload initialization failed.", e)
                        DomainResult.Error(NetworkError.SERVER_ERROR)
                    }
                }
            } else {
                Log.e("ImageUploadWorker", "resumeOrCreateUpload failed with code: ${e.causingConnection?.responseCode}", e)
                return DomainResult.Error(NetworkError.SERVER_ERROR)
            }
        } catch (e: IOException) {
            Log.e("ImageUploadWorker", "resumeOrCreateUpload failed with IOException", e)
            return DomainResult.Error(NetworkError.NO_INTERNET)
        }

        val uploader = uploaderResult.successOrNull() ?: return DomainResult.Error(NetworkError.UNKNOWN_ERROR)

        executeUploadLoop(uploader, tusClient, upload, sessionId, currentIndex, totalImages, shortSessionId).onError {
            return DomainResult.Error(it)
        }

        return when (val finalUrlResult = safeFinish(uploader, uploadSpecimenId, md5)) {
            is DomainResult.Success -> {
                updateSessionProgressNotification(shortSessionId, currentIndex, totalImages, "Verifying...")
                Log.d("ImageUploadWorker", "Upload finished successfully: ${finalUrlResult.data}")
                DomainResult.Success(finalUrlResult.data.toString())
            }
            is DomainResult.Error -> DomainResult.Error(finalUrlResult.error)
        }
    }

    private suspend fun executeUploadLoop(initialUploader: TusUploader, client: TusClient, upload: TusUpload, sessionId: UUID, currentIndex: Int, totalImages: Int, shortSessionId: String): DomainResult<Unit, NetworkError> {
        var uploader = initialUploader
        var currentChunkSize = INITIAL_CHUNK_SIZE_BYTES
        var successfulUploadsInARow = 0

        while (uploader.offset < upload.size) {
            uploader.chunkSize = currentChunkSize
            uploader.requestPayloadSize = currentChunkSize

            val chunkResult = try {
                withTimeout(NETWORK_TIMEOUT_MS) { DomainResult.Success(uploader.uploadChunk()) }
            } catch (e: Exception) {
                when (e) {
                    is TimeoutCancellationException -> DomainResult.Error(NetworkError.REQUEST_TIMEOUT)
                    is IOException -> DomainResult.Error(NetworkError.NO_INTERNET)
                    is io.tus.java.client.ProtocolException -> DomainResult.Error(NetworkError.SERVER_ERROR)
                    else -> DomainResult.Error(NetworkError.UNKNOWN_ERROR)
                }
            }

            when (chunkResult) {
                is DomainResult.Success -> {
                    val bytesUploaded = chunkResult.data
                    if (bytesUploaded <= -1) {
                        Log.w("ImageUploadWorker", "File stream ended unexpectedly.")
                        break
                    }
                    val percent = if (upload.size > 0) (uploader.offset * 100 / upload.size).toInt() else 0
                    updateSessionProgressNotification(shortSessionId, currentIndex, totalImages, "$percent%")
                    setProgress(workDataOf(KEY_PROGRESS_UPLOADED to uploader.offset, KEY_PROGRESS_TOTAL to upload.size))

                    successfulUploadsInARow++
                    if (successfulUploadsInARow >= SUCCESS_STREAK_FOR_INCREASE) {
                        currentChunkSize = (currentChunkSize * SUCCESS_CHUNK_SIZE_MULTIPLIER).coerceAtMost(MAX_CHUNK_SIZE_BYTES)
                        Log.i("ImageUploadWorker", "Increasing chunk size to $currentChunkSize bytes.")
                        successfulUploadsInARow = 0
                    }
                }
                is DomainResult.Error -> {
                    val error = chunkResult.error
                    successfulUploadsInARow = 0
                    currentChunkSize = (currentChunkSize / FAILURE_CHUNK_SIZE_DIVIDER).coerceAtLeast(MIN_CHUNK_SIZE_BYTES)
                    Log.w("ImageUploadWorker", "Upload error: $error. Reducing chunk size to $currentChunkSize bytes.")

                    if (error == NetworkError.REQUEST_TIMEOUT || error == NetworkError.NO_INTERNET || error == NetworkError.SERVER_ERROR) {
                        try {
                            uploader = client.resumeOrCreateUpload(upload)
                            continue
                        } catch (resumeException: Exception) {
                            return DomainResult.Error(NetworkError.UNKNOWN_ERROR)
                        }
                    } else {
                        return DomainResult.Error(error)
                    }
                }
            }
        }
        Log.d("ImageUploadWorker", "Upload loop finished. Final offset: ${uploader.offset}")
        return DomainResult.Success(Unit)
    }

    private fun getSessionId(): UUID? {
        return try {
            val sessionIdStr = inputData.getString(KEY_SESSION_ID) ?: return null
            UUID.fromString(sessionIdStr)
        } catch (e: IllegalArgumentException) {
            Log.e("ImageUploadWorker", "Invalid session ID provided.", e)
            null
        }
    }

    private suspend fun updateStatus(specimenId: String, sessionId: UUID, status: UploadStatus) {
        try {
            val specimen = specimenRepository.getSpecimenById(specimenId)
            specimen?.let {
                val updatedSpecimen = it.copy(imageUploadStatus = status)
                specimenRepository.updateSpecimen(updatedSpecimen, sessionId)
                Log.d("ImageUploadWorker", "Updated status for specimen $specimenId to $status")
            } ?: Log.w("ImageUploadWorker", "Could not find specimen $specimenId to update status.")
        } catch (e: Exception) {
            Log.e("ImageUploadWorker", "Failed to update specimen status for $specimenId", e)
        }
    }

    private fun createTusUpload(file: File, fingerprint: String, contentType: String?, md5: String) = TusUpload(file).apply {
        this.fingerprint = fingerprint
        setMetadata(mapOf(
            "filename" to file.name,
            "contentType" to (contentType ?: "application/octet-stream"),
            "filemd5" to md5
        ))
    }

    private fun calculateMD5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private suspend fun prepareFile(resolver: ContentResolver, source: Uri): Pair<File, String?> = withContext(Dispatchers.IO) {
        val mimeType = resolver.getType(source)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "bin"
        }
        val destination = File(context.cacheDir, "work_upload_${System.currentTimeMillis()}.$extension")
        resolver.openInputStream(source)?.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to open $source")
        Log.d("ImageUploadWorker", "Prepared ${destination.name} (${destination.length()} bytes)")
        return@withContext destination to mimeType
    }

    private suspend fun safeFinish(uploader: TusUploader, specimenId: String, md5: String): DomainResult<URL, NetworkError> = withContext(Dispatchers.IO) {
        try {
            uploader.finish()
            Log.d("ImageUploadWorker", "Tus finish() successful. Verifying with MD5 check.")
            when (val verificationResult = imageUploadDataSource.imageExists(specimenId, md5)) {
                is DomainResult.Success -> verificationResult
                is DomainResult.Error -> DomainResult.Error(NetworkError.VERIFICATION_ERROR)
            }
        } catch (e: io.tus.java.client.ProtocolException) {
            Log.w("ImageUploadWorker", "finish() failed. Fallback to MD5 check.", e)
            when (val verificationResult = imageUploadDataSource.imageExists(specimenId, md5)) {
                is DomainResult.Success -> verificationResult
                is DomainResult.Error -> DomainResult.Error(NetworkError.VERIFICATION_ERROR)
            }
        } catch (e: IOException) {
            Log.e("ImageUploadWorker", "finish() failed due to IOException", e)
            DomainResult.Error(NetworkError.NO_INTERNET)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showInitialSessionNotification(sessionId: String, total: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Session: $sessionId")
            .setContentText("Preparing to upload $total images…")
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(total, 0, true)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun updateSessionProgressNotification(sessionId: String, currentIndex: Int, totalImages: Int, progressText: String) {
        val filesCompleted = currentIndex - 1

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Session: $sessionId")
            .setContentText("Uploading image $currentIndex of $totalImages")
            .setSubText(progressText)
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(totalImages, filesCompleted, false)
            .setOngoing(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showFinalStatusNotification(sessionId: String, successful: Int, total: Int) {
        val title = if (successful == total) "Upload complete" else "Upload error"
        val message = "Session $sessionId: $successful of $total images uploaded."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
