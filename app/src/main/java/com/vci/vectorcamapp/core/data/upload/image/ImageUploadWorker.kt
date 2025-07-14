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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.tus.android.client.TusPreferencesURLStore
import io.tus.java.client.TusClient
import io.tus.java.client.TusUpload
import io.tus.java.client.TusUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.security.MessageDigest
import androidx.work.ListenableWorker.Result as WorkerResult

@HiltWorker
class ImageUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_URI          = "uri"
        const val KEY_ENDPOINT     = "endpoint"
        const val KEY_FINGERPRINT  = "fingerprint"

        const val KEY_UPLOAD_URL   = "upload_url"

        const val KEY_PROGRESS_UPLOADED = "progress_uploaded"
        const val KEY_PROGRESS_TOTAL    = "progress_total"

        const val CHUNK_SIZE_KB        = 64
        const val REQUEST_PAYLOAD_KB   = 256

        const val CHANNEL_ID    = "image_upload_channel"
        const val CHANNEL_NAME  = "Image Upload Channel"
        const val NOTIFICATION_ID = 1001
        const val STATUS_NOTIFICATION_ID = 1002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): WorkerResult {
        val uriStr   = inputData.getString(KEY_URI)      ?: return WorkerResult.failure()
        val endpoint = inputData.getString(KEY_ENDPOINT) ?: return WorkerResult.failure()
        val srcUri   = uriStr.toUri()

        createNotificationChannel()
        setForeground(showInitialNotification())

        var tmpFile: File? = null

        return try {
            val prep = prepareFile(context.contentResolver, srcUri)
            tmpFile = prep.first
            val contentType = prep.second
            val md5 = calculateMD5(tmpFile)

            val checkUrl = getCheckUrl(endpoint, md5)

            Log.d("ImageUploadWorker", "Checking if image with MD5 $md5 already exists at $checkUrl")
            val (exists, existingUrl) = imageExists(checkUrl)
            if (exists && existingUrl != null) {
                Log.d("ImageUploadWorker", "Image already exists on server. Skipping upload. URL: $existingUrl")
                notificationManager.cancel(NOTIFICATION_ID)
                return WorkerResult.success(
                    workDataOf(KEY_UPLOAD_URL to existingUrl.toString())
                )
            }
            Log.d("ImageUploadWorker", "Image does not exist. Proceeding with upload.")


            val client = TusClient().apply {
                setUploadCreationURL(URL(endpoint))
                enableResuming(
                    TusPreferencesURLStore(
                        context.getSharedPreferences("tus_worker", Context.MODE_PRIVATE)
                    )
                )
                setHeaders(mapOf("Content-Type" to "application/offset+octet-stream"))
            }

            val upload = TusUpload(tmpFile).apply {
                this.fingerprint = md5
                setMetadata(
                    mapOf(
                        "filename"    to tmpFile.name,
                        "contentType" to (contentType ?: "application/octet-stream"),
                        "filemd5"     to md5
                    )
                )
            }

            Log.d("ImageUploadWorker", "Start/resume $tmpFile (fp=$md5, md5=$md5)")

            val uploader = client.resumeOrCreateUpload(upload).apply {
                setChunkSize(CHUNK_SIZE_KB * 1024)
                setRequestPayloadSize(REQUEST_PAYLOAD_KB * 1024)
            }

            updateWorkerProgress(uploader.offset, upload.size)

            while (true) {
                val chunkStart = System.currentTimeMillis()
                val bytes = uploader.uploadChunk()
                if (bytes <= -1) break

                val chunkEnd   = System.currentTimeMillis()
                val deltaMs    = (chunkEnd - chunkStart).coerceAtLeast(1L)
                val bytesPerSec = bytes * 1000 / deltaMs

                Log.d("ImageUploadWorker", "Chunked $bytes bytes in $deltaMs ms → $bytesPerSec B/s")
                updateWorkerProgress(uploader.offset, upload.size)
            }

            val url = safeFinish(uploader, checkUrl).toString()
            Log.d("ImageUploadWorker", "Finish OK: $url")

            updateWorkerProgress(upload.size, upload.size)
            notificationManager.cancel(NOTIFICATION_ID)

            return WorkerResult.success(
                workDataOf(KEY_UPLOAD_URL to url)
            )

        } catch (e: ProtocolException) {
            Log.e("ImageUploadWorker", "Permanent failure", e)
            notificationManager.cancel(NOTIFICATION_ID)
            showUploadErrorNotification("Permanent protocol error.")
            WorkerResult.failure()

        } catch (e: IOException) {
            Log.w("ImageUploadWorker", "Transient failure", e)
            notificationManager.cancel(NOTIFICATION_ID)
            showUploadErrorNotification("Internet connection lost, attempting reconnection.")
            WorkerResult.retry()

        } catch (e: Exception) {
            Log.e("ImageUploadWorker", "Unexpected error", e)
            notificationManager.cancel(NOTIFICATION_ID)
            showUploadErrorNotification("Unexpected error.")
            WorkerResult.failure()
        } finally {
            tmpFile?.delete()
        }
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
        val digest = md.digest()
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun getCheckUrl(tusEndpoint: String, md5: String): URL {
        val baseUrl = tusEndpoint.removeSuffix("/tus")
        return URL("$baseUrl/$md5")
    }

    private suspend fun imageExists(url: URL): Pair<Boolean, URL?> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.instanceFollowRedirects = true
                connection.connect()
                val responseCode = connection.responseCode
                val finalUrl = connection.url
                (responseCode == HttpURLConnection.HTTP_OK) to finalUrl
            } catch (e: IOException) {
                Log.w("ImageUploadWorker", "Image existence check failed for $url", e)
                false to null
            }
        }
    }

    private suspend fun prepareFile(resolver: ContentResolver, src: Uri): Pair<File, String?> =
        withContext(Dispatchers.IO) {
            val mimeType = resolver.getType(src)
            Log.d("ImageUploadWorker", "Preparing file from URI: $src with MIME type: $mimeType")

            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> "bin"
            }
            val safeName = "work_upload_${System.currentTimeMillis()}.$extension"
            val dst = File(context.cacheDir, safeName)

            resolver.openInputStream(src)?.use { inStream ->
                FileOutputStream(dst).use { outStream ->
                    inStream.copyTo(outStream)
                }
            } ?: throw IOException("Unable to open $src")

            val fileSizeInBytes = dst.length()
            Log.d("ImageUploadWorker", "Prepared file size: $fileSizeInBytes bytes")
            return@withContext dst to mimeType
        }

    @Throws(io.tus.java.client.ProtocolException::class, IOException::class)
    private suspend fun safeFinish(
        uploader: TusUploader,
        checkUrl: URL
    ): URL = withContext(Dispatchers.IO) {
        try {
            uploader.finish()
            return@withContext uploader.uploadURL
        } catch (e: Exception) {
            Log.w("ImageUploadWorker", "finish() failed (${e.javaClass.simpleName}). " +
                    "Verifying with server via MD5 check...")

            val (exists, existingUrl) = imageExists(checkUrl)
            if (exists && existingUrl != null) {
                Log.d("ImageUploadWorker", "Server has the file (verified by MD5 check), treating as success.")
                return@withContext existingUrl
            } else {
                Log.e("ImageUploadWorker", "MD5 check also failed after upload failure. Marking as failed.", e)
                throw e
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun showInitialNotification(): ForegroundInfo {
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading image")
            .setContentText("Preparing upload…")
            .setSmallIcon(R.drawable.ic_upload)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID,
            n,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private suspend fun updateWorkerProgress(uploaded: Long, total: Long) {
        setProgress(workDataOf(
            KEY_PROGRESS_UPLOADED to uploaded,
            KEY_PROGRESS_TOTAL    to total
        ))

        val percent = if (total > 0) (uploaded * 100 / total).toInt() else 0
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading image")
            .setContentText("$percent %")
            .setSmallIcon(R.drawable.ic_upload)
            .setProgress(100, percent, false)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, n)
    }

    private fun showUploadErrorNotification(message: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Upload failed")
            .setSmallIcon(R.drawable.ic_upload)
            .setContentText(message)
            .build()
        notificationManager.notify(STATUS_NOTIFICATION_ID, n)
    }
}
