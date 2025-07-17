package com.vci.vectorcamapp.core.data.upload.image

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.tus.android.client.TusPreferencesURLStore
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

@HiltWorker
class ImageUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val specimenRepository: SpecimenRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_URI = "uri"
        const val KEY_ENDPOINT = "endpoint"
        const val KEY_UPLOAD_URL = "upload_url"
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

        private const val CHANNEL_ID = "image_upload_channel"
        private const val CHANNEL_NAME = "Image Upload Channel"
        private const val NOTIFICATION_ID = 1001
        private const val STATUS_NOTIFICATION_ID = 1002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private data class UploadInput(val uri: Uri, val endpoint: String, val specimenId: String, val sessionId: UUID)

    override suspend fun doWork(): WorkerResult {
        val input = getUploadInput() ?: return WorkerResult.failure()
        updateStatus(input.specimenId, input.sessionId, UploadStatus.IN_PROGRESS)
        createNotificationChannel()
        setForeground(showInitialNotification())

        var temporaryFile: File? = null

        return try {
            val (file, contentType) = prepareFile(context.contentResolver, input.uri)
            temporaryFile = file

            val uploadUrl = performUpload(input, file, contentType)

            notificationManager.cancel(NOTIFICATION_ID)

            updateStatus(input.specimenId, input.sessionId, UploadStatus.COMPLETED)

            WorkerResult.success(workDataOf(KEY_UPLOAD_URL to uploadUrl))
        } catch (e: IOException) {
            Log.w("ImageUploadWorker", "Transient I/O failure, will retry.", e)
            showUploadErrorNotification("Connection issue, retrying...")
            updateStatus(input.specimenId, input.sessionId, UploadStatus.RETRY)
            WorkerResult.retry()
        } catch (e: Exception) {
            val responseCode = (e as? io.tus.java.client.ProtocolException)?.causingConnection?.responseCode
            Log.e("ImageUploadWorker", "Permanent or unexpected failure (Code: $responseCode).", e)
            showUploadErrorNotification("Upload failed: ${e.message}")
            updateStatus(input.specimenId, input.sessionId, UploadStatus.RETRY)
            WorkerResult.failure()
        } finally {
            temporaryFile?.delete()
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private suspend fun performUpload(input: UploadInput, file: File, contentType: String?): String {
        val md5 = calculateMD5(file)
        val uniqueFingerprint = "${input.specimenId}-$md5"
        val checkUrl = getCheckUrl(input.endpoint, md5)

        val client = createTusClient(input.endpoint)
        val upload = createTusUpload(file, uniqueFingerprint, contentType, md5)

        Log.d("ImageUploadWorker", "Start/resume ${file.name} (fp=$uniqueFingerprint,md5=$md5)")

        val uploader = try {
            client.resumeOrCreateUpload(upload)
        } catch (e: io.tus.java.client.ProtocolException) {
            if (e.causingConnection?.responseCode == HttpURLConnection.HTTP_CONFLICT) {
                Log.w("ImageUploadWorker", "resumeOrCreateUpload failed with 409 Conflict. Verifying if file already exists on server.", e)
                val existingUrl = verifyUploadByMD5(checkUrl)
                if (existingUrl != null) {
                    Log.d("ImageUploadWorker", "Image already exists on server. URL: $existingUrl")
                    return existingUrl.toString()
                } else {
                    Log.e("ImageUploadWorker", "File does not exist on server despite 409. Upload initialization truly failed.", e)
                    throw e
                }
            } else {
                Log.e("ImageUploadWorker", "resumeOrCreateUpload failed with unhandled status code: ${e.causingConnection?.responseCode}", e)
                throw e
            }
        }

        executeUploadLoop(uploader, client, upload)

        val finalUrl = safeFinish(uploader, checkUrl)
        updateWorkerProgress(upload.size, upload.size)
        Log.d("ImageUploadWorker", "Upload finished successfully: $finalUrl")
        return finalUrl.toString()
    }

    private suspend fun executeUploadLoop(initialUploader: TusUploader, client: TusClient, upload: TusUpload) {
        var uploader = initialUploader
        var currentChunkSize = INITIAL_CHUNK_SIZE_BYTES
        var successfulUploadsInARow = 0

        while (uploader.offset < upload.size) {
            uploader.chunkSize = currentChunkSize
            uploader.requestPayloadSize = currentChunkSize

            val bytesUploaded = try {
                withTimeout(NETWORK_TIMEOUT_MS) { uploader.uploadChunk() }
            } catch (e: Exception) {
                successfulUploadsInARow = 0
                currentChunkSize = (currentChunkSize / FAILURE_CHUNK_SIZE_DIVIDER).coerceAtLeast(MIN_CHUNK_SIZE_BYTES)
                Log.w("ImageUploadWorker", "Upload error. Reducing chunk size to $currentChunkSize bytes.", e)

                val isRetryable = when (e) {
                    is TimeoutCancellationException, is IOException -> true
                    is io.tus.java.client.ProtocolException ->
                        e.causingConnection?.responseCode == HttpURLConnection.HTTP_CONFLICT
                    else -> false
                }

                if (isRetryable) {
                    uploader = client.resumeOrCreateUpload(upload)
                    continue
                } else {
                    throw e
                }
            }

            if (bytesUploaded <= -1) {
                Log.w("ImageUploadWorker", "File stream ended unexpectedly.")
                break
            }

            updateWorkerProgress(uploader.offset, upload.size)
            Log.d("ImageUploadWorker", "Uploaded a payload of $bytesUploaded bytes.")

            successfulUploadsInARow++
            if (successfulUploadsInARow >= SUCCESS_STREAK_FOR_INCREASE) {
                currentChunkSize = (currentChunkSize * SUCCESS_CHUNK_SIZE_MULTIPLIER).coerceAtMost(MAX_CHUNK_SIZE_BYTES)
                Log.i("ImageUploadWorker", "Stable connection. Increasing chunk and payload size to $currentChunkSize bytes.")
                successfulUploadsInARow = 0
            }
        }
        Log.d("ImageUploadWorker", "Upload loop finished. Final offset: ${uploader.offset}")
    }

    private fun getUploadInput(): UploadInput? {
        return try {
            val uriStr = inputData.getString(KEY_URI) ?: return null
            val endpoint = inputData.getString(KEY_ENDPOINT) ?: return null
            val specimenId = inputData.getString(KEY_SPECIMEN_ID) ?: return null
            val sessionIdStr = inputData.getString(KEY_SESSION_ID) ?: return null
            val sessionId = UUID.fromString(sessionIdStr)
            UploadInput(uriStr.toUri(), endpoint, specimenId, sessionId)
        } catch (e: IllegalArgumentException) {
            Log.e("ImageUploadWorker", "Invalid input data provided.", e)
            null
        }
    }

    private fun createTusClient(endpoint: String): TusClient = TusClient().apply {
        setUploadCreationURL(URL(endpoint))
        enableResuming(TusPreferencesURLStore(context.getSharedPreferences("tus_worker", Context.MODE_PRIVATE)))
        setHeaders(mapOf("Content-Type" to "application/offset+octet-stream"))
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

    private fun getCheckUrl(tusEndpoint: String, md5: String): URL {
        val baseUrl = tusEndpoint.removeSuffix("/tus")
        return URL("$baseUrl/$md5")
    }

    private suspend fun imageExists(url: URL): Pair<Boolean, URL?> = withContext(Dispatchers.IO) {
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            val responseCode = connection.responseCode
            val finalUrl = connection.url
            (responseCode == HttpURLConnection.HTTP_OK) to finalUrl
        } catch (e: IOException) {
            Log.w("ImageUploadWorker", "Image existence check failed for $url", e)
            false to null
        }
    }

    private suspend fun prepareFile(resolver: ContentResolver, src: Uri): Pair<File, String?> = withContext(Dispatchers.IO) {
        val mimeType = resolver.getType(src)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "bin"
        }
        val dst = File(context.cacheDir, "work_upload_${System.currentTimeMillis()}.$extension")
        resolver.openInputStream(src)?.use { input ->
            FileOutputStream(dst).use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to open $src")
        Log.d("ImageUploadWorker", "Prepared ${dst.name} (${dst.length()} bytes)")
        return@withContext dst to mimeType
    }

    private suspend fun verifyUploadByMD5(checkUrl: URL): URL? {
        val (exists, existingUrl) = imageExists(checkUrl)
        return if (exists && existingUrl != null) {
            Log.d("ImageUploadWorker", "Server has the file (verified by MD5 check). Success.")
            existingUrl
        } else {
            null
        }
    }

    @Throws(io.tus.java.client.ProtocolException::class, IOException::class)
    private suspend fun safeFinish(uploader: TusUploader, checkUrl: URL): URL = withContext(Dispatchers.IO) {
        try {
            uploader.finish()
            Log.d("ImageUploadWorker", "Tus finish() successful. Verifying with MD5 check.")

            val verifiedUrl = verifyUploadByMD5(checkUrl)
            if (verifiedUrl != null) {
                Log.d("ImageUploadWorker", "MD5 verification successful after finish(). Final URL: $verifiedUrl")
                return@withContext verifiedUrl
            } else {
                Log.e("ImageUploadWorker", "CRITICAL: finish() succeeded but MD5 check failed for ${checkUrl}.")
                throw IOException("Upload verification failed: file not found on server after successful finish.")
            }
        } catch (e: io.tus.java.client.ProtocolException) {
            Log.w("ImageUploadWorker", "finish() failed. Verifying with server via MD5 check...", e)
            val existingUrl = verifyUploadByMD5(checkUrl)
            if (existingUrl != null) {
                Log.d("ImageUploadWorker", "Image already exists on server. URL: $existingUrl")
                return@withContext existingUrl
            } else {
                Log.e("ImageUploadWorker", "File does not exist on server. Upload initialization truly failed.", e)
                throw e
            }
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