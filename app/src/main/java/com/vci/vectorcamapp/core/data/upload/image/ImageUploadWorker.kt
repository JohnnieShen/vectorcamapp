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
    private val imageUploadDataSource: ImageUploadDataSource,
    private val tusClient: TusClient
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SPECIMEN_ID = "specimen_id"
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
        private const val STATUS_NOTIFICATION_ID = 1002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private data class UploadInput(val specimenId: String, val sessionId: UUID)

    override suspend fun doWork(): WorkerResult {
        val input = getUploadInput() ?: return WorkerResult.failure()
        updateStatus(input.specimenId, input.sessionId, UploadStatus.IN_PROGRESS)
        createNotificationChannel()
        setForeground(showInitialNotification())

        var temporaryFile: File? = null

        val specimenToUpload = specimenRepository.getSpecimenById(input.specimenId) ?: return WorkerResult.failure()

        return try {
            val (file, contentType) = prepareFile(context.contentResolver, specimenToUpload.imageUri)
            temporaryFile = file

            return when (val result = performUpload(input, file, contentType)) {
                is DomainResult.Success -> {
                    notificationManager.cancel(NOTIFICATION_ID)
                    updateStatus(input.specimenId, input.sessionId, UploadStatus.COMPLETED)
                    WorkerResult.success()
                }
                is DomainResult.Error -> {
                    handleUploadError(result.error, input)
                }
            }
        } catch (e: IOException) {
            Log.w("ImageUploadWorker", "Transient I/O failure, will retry.", e)
            return handleUploadError(NetworkError.NO_INTERNET, input)
        } catch (e: Exception) {
            val responseCode = (e as? io.tus.java.client.ProtocolException)?.causingConnection?.responseCode
            Log.e("ImageUploadWorker", "Permanent or unexpected failure (Code: $responseCode).", e)
            return handleUploadError(NetworkError.UNKNOWN_ERROR, input)
        } finally {
            temporaryFile?.delete()
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private suspend fun handleUploadError(error: NetworkError, input: UploadInput): WorkerResult {
        val isRetryable = when (error) {
            NetworkError.NO_INTERNET,
            NetworkError.REQUEST_TIMEOUT -> true
            else -> false
        }

        val errorMessage = when (error) {
            NetworkError.NO_INTERNET -> "Connection issue, retrying..."
            NetworkError.REQUEST_TIMEOUT -> "Connection timed out, retrying..."
            NetworkError.SERVER_ERROR -> "Server protocol error."
            NetworkError.VERIFICATION_ERROR -> "Upload failed: file could not be verified."
            NetworkError.UNKNOWN_ERROR -> "An unknown upload error occurred."
            else -> "An unknown upload error occurred."
        }

        if (isRetryable && runAttemptCount < MAX_RETRIES) {
            Log.w("ImageUploadWorker", "Retryable error occurred: $error. Attempt ${runAttemptCount + 1}.")
            showUploadErrorNotification(errorMessage)
            updateStatus(input.specimenId, input.sessionId, UploadStatus.FAILED)
            return WorkerResult.retry()
        } else {
            Log.e("ImageUploadWorker", "Permanent or max-retry error: $error, $errorMessage.")
            showUploadErrorNotification(errorMessage)
            updateStatus(input.specimenId, input.sessionId, UploadStatus.FAILED)
            return WorkerResult.failure()
        }
    }

    private suspend fun performUpload(input: UploadInput, file: File, contentType: String?): DomainResult<String, NetworkError> {
        val md5 = calculateMD5(file)
        val uniqueFingerprint = "${input.specimenId}-$md5"

        val tusPath = "specimens/${input.specimenId}/images/tus"

        tusClient.uploadCreationURL = URL(constructUrl(tusPath))
        val upload = createTusUpload(file, uniqueFingerprint, contentType, md5)

        Log.d("ImageUploadWorker", "Start/resume ${file.name} (fp=$uniqueFingerprint,md5=$md5)")

        val uploaderResult = try {
            DomainResult.Success(tusClient.resumeOrCreateUpload(upload))
        } catch (e: io.tus.java.client.ProtocolException) {
            if (e.causingConnection?.responseCode == HttpURLConnection.HTTP_CONFLICT) {
                Log.w("ImageUploadWorker", "resumeOrCreateUpload failed with 409 Conflict. Verifying by MD5.", e)
                return when (val existingUrlResult = imageUploadDataSource.imageExists(input.specimenId, md5)) {
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
            return DomainResult.Error(NetworkError.NO_INTERNET)
        }

        val uploader = uploaderResult.successOrNull() ?: return DomainResult.Error(NetworkError.UNKNOWN_ERROR)

        executeUploadLoop(uploader, tusClient, upload).onError {
            return DomainResult.Error(it)
        }

        return when (val finalUrlResult = safeFinish(uploader, input.specimenId, md5)) {
            is DomainResult.Success -> {
                updateWorkerProgress(upload.size, upload.size)
                Log.d("ImageUploadWorker", "Upload finished successfully: ${finalUrlResult.data}")
                DomainResult.Success(finalUrlResult.data.toString())
            }
            is DomainResult.Error -> DomainResult.Error(finalUrlResult.error)
        }
    }

    private suspend fun executeUploadLoop(initialUploader: TusUploader, client: TusClient, upload: TusUpload): DomainResult<Unit, NetworkError> {
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

                    updateWorkerProgress(uploader.offset, upload.size)
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

    private fun getUploadInput(): UploadInput? {
        return try {
            val specimenId = inputData.getString(KEY_SPECIMEN_ID) ?: return null
            val sessionIdStr = inputData.getString(KEY_SESSION_ID) ?: return null
            val sessionId = UUID.fromString(sessionIdStr)
            UploadInput(specimenId, sessionId)
        } catch (e: IllegalArgumentException) {
            Log.e("ImageUploadWorker", "Invalid input data provided.", e)
            null
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
            DomainResult.Error(NetworkError.NO_INTERNET)
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showInitialNotification(): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading image")
            .setContentText("Preparing upload…")
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private suspend fun updateWorkerProgress(uploaded: Long, total: Long) {
        setProgress(workDataOf(KEY_PROGRESS_UPLOADED to uploaded, KEY_PROGRESS_TOTAL to total))
        val percent = if (total > 0) (uploaded * 100 / total).toInt() else 0
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading image")
            .setContentText("$percent %")
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(100, percent, false)
            .setOngoing(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showUploadErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Upload failed")
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setContentText(message)
            .build()
        notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
    }
}
