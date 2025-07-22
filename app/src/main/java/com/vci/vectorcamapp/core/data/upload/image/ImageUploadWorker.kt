package com.vci.vectorcamapp.core.data.upload.image

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.network.api.SpecimenImageDataSource
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.tus.java.client.ProtocolException
import io.tus.java.client.TusClient
import io.tus.java.client.TusUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import androidx.work.ListenableWorker.Result as WorkerResult
import com.vci.vectorcamapp.core.domain.util.Result as DomainResult

@HiltWorker
class ImageUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val specimenRepository: SpecimenRepository,
    private val sessionRepository: SessionRepository,
    private val specimenImageDataSource: SpecimenImageDataSource,
    private val tusClient: TusClient
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SESSION_ID = "session_id"

        private const val MD5_BUFFER_SIZE = 8 * 1024

        private const val INITIAL_CHUNK_SIZE = 64 * 1024
        private const val MIN_CHUNK_SIZE = 16 * 1024
        private const val MAX_CHUNK_SIZE = 1024 * 1024
        private const val SUCCESS_STREAK_THRESHOLD = 5
        private const val FAILURE_STREAK_THRESHOLD = 5
        private const val CHUNK_SIZE_MULTIPLIER = 2
        private const val CHUNK_SIZE_DIVIDER = 2

        private const val CHANNEL_ID = "image_upload_channel"
        private const val CHANNEL_NAME = "Image Upload Channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): WorkerResult {
        createNotificationChannel()
        val localSessionIdString =
            inputData.getString(KEY_SESSION_ID) ?: return WorkerResult.failure()

        val localSessionId = try {
            UUID.fromString(localSessionIdString)
        } catch (e: IllegalArgumentException) {
            return WorkerResult.failure()
        }

        val sessionWithSpecimens = sessionRepository.getSessionWithSpecimensById(localSessionId)
            ?: return WorkerResult.failure()
        val session = sessionWithSpecimens.session

        val specimensToUpload =
            sessionWithSpecimens.specimens.filter { it.imageUploadStatus != UploadStatus.COMPLETED }
        if (specimensToUpload.isEmpty()) return WorkerResult.success()

        setForeground(showInitialImageNotification(session, specimensToUpload.size))

        var anyPermanentFailures = false
        var anyTransientFailures = false
        specimensToUpload.forEachIndexed { index, specimen ->
            try {
                val (file, mimeType, md5) = prepareFileFromUri(specimen.imageUri, specimen.id)

                val specimenId = "SPE001" // TODO: HARDCODED FOR NOW. CHANGE LATER
                val imageId = 50 // TODO: HARDCODED FOR NOW. CHANGE LATER
                val checkImageExistsResult =
                    specimenImageDataSource.checkImageExists(specimenId, imageId)
                if (checkImageExistsResult is DomainResult.Success) {
                    val remoteMd5 = checkImageExistsResult.data.filemd5
                    if (remoteMd5.equals(md5, ignoreCase = true)) {
                        specimenRepository.updateSpecimen(
                            specimen.copy(imageUploadStatus = UploadStatus.COMPLETED),
                            session.localId
                        )
                        updateImageUploadProgress(session, index + 1, specimensToUpload.size)
                        file.delete()
                        return@forEachIndexed
                    }
                }

                tusClient.uploadCreationURL = URL(constructUrl("specimens/$specimenId/images/tus"))
                val uploadFingerprint = "${specimenId}-${imageId}-${md5}"
                val metadata = mapOf(
                    "filename" to file.name,
                    "contentType" to mimeType,
                    "filemd5" to md5,
                    "imageId" to imageId.toString()
                )
                val upload = createTusUpload(file, uploadFingerprint, metadata)
                var uploader = tusClient.resumeOrCreateUpload(upload)

                var chunkSize = INITIAL_CHUNK_SIZE
                var successStreak = 0
                var failureStreak = 0

                specimenRepository.updateSpecimen(
                    specimen.copy(imageUploadStatus = UploadStatus.IN_PROGRESS),
                    session.localId
                )

                while (uploader.offset < file.length()) {
                    try {
                        uploader.chunkSize = chunkSize
                        val bytesUploaded = uploader.uploadChunk()
                        if (bytesUploaded <= -1) throw IOException("Unexpected EOF")

                        successStreak++
                        failureStreak = 0

                        if (successStreak >= SUCCESS_STREAK_THRESHOLD) {
                            chunkSize =
                                (chunkSize * CHUNK_SIZE_MULTIPLIER).coerceAtMost(MAX_CHUNK_SIZE)
                            successStreak = 0
                        }
                    } catch (e: Throwable) {
                        failureStreak++
                        successStreak = 0

                        if (isRetriableException(e)) {
                            chunkSize =
                                (chunkSize / CHUNK_SIZE_DIVIDER).coerceAtLeast(MIN_CHUNK_SIZE)

                            if (failureStreak >= FAILURE_STREAK_THRESHOLD) {
                                file.delete()
                                throw e
                            }

                            uploader = tusClient.resumeOrCreateUpload(upload)
                        } else {
                            file.delete()
                            throw e
                        }
                    }
                }

                uploader.finish()
                specimenRepository.updateSpecimen(
                    specimen.copy(imageUploadStatus = UploadStatus.COMPLETED),
                    session.localId
                )

                file.delete()
            } catch (e: Exception) {
                specimenRepository.updateSpecimen(
                    specimen.copy(imageUploadStatus = UploadStatus.FAILED),
                    session.localId
                )
                if (isRetriableException(e)) {
                    anyTransientFailures = true // SHOULD YOU RETRY AT THIS POINT????
                } else {
                    anyPermanentFailures = true
                }
            }
        }

        return when {
            anyPermanentFailures -> WorkerResult.failure()
            anyTransientFailures -> WorkerResult.retry()
            else -> WorkerResult.success()
        }
    }

    private suspend fun prepareFileFromUri(
        uri: Uri,
        specimenId: String
    ): Triple<File, String, String> {
        return withContext(Dispatchers.IO) {
            val mimeType = context.contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> "bin"
            }

            val fileName = "upload_specimen_${specimenId}.$extension"
            val file = File(context.cacheDir, fileName)

            if (!file.exists()) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IOException("Failed to open URI: $uri")
            }

            val md5 = calculateMd5(file)
            Triple(file, mimeType ?: "application/octet-stream", md5)
        }
    }

    private fun calculateMd5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(MD5_BUFFER_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun createTusUpload(
        file: File,
        fingerprint: String,
        metadata: Map<String, String>
    ): TusUpload {
        return TusUpload(file).apply {
            this.fingerprint = fingerprint
            this.metadata = metadata
        }
    }

    private fun isRetriableException(e: Throwable): Boolean {
        return e is SocketTimeoutException || (e is ProtocolException && e.shouldRetry()) || e is IOException
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showInitialImageNotification(session: Session, total: Int): ForegroundInfo {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading Session from ${dateFormatter.format(session.createdAt)}")
            .setContentText("Preparing to upload $total images…")
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(total, 0, true)
            .setOngoing(true)
            .build()
        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun updateImageUploadProgress(session: Session, current: Int, total: Int) {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Uploading Session from ${dateFormatter.format(session.createdAt)}")
            .setContentText("Uploading image $current of $total…")
            .setSmallIcon(R.drawable.ic_cloud_upload)
            .setProgress(total, current, false)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}


//
//@HiltWorker
//class ImageUploadWorker @AssistedInject constructor(
//    @Assisted private val context: Context,
//    @Assisted workerParams: WorkerParameters,
//    private val specimenRepository: SpecimenRepository,
//    private val sessionRepository: SessionRepository,
//    private val tusClient: TusClient
//) : CoroutineWorker(context, workerParams) {
//
//    companion object {
//        const val KEY_SESSION_ID = "session_id"
//
//        const val KEY_PROGRESS_UPLOADED = "progress_uploaded"
//        const val KEY_PROGRESS_TOTAL = "progress_total"
//
//        private const val INITIAL_CHUNK_SIZE_BYTES = 64 * 1024
//        private const val MIN_CHUNK_SIZE_BYTES = 16 * 1024
//        private const val MAX_CHUNK_SIZE_BYTES = 1024 * 1024
//        private const val SUCCESS_STREAK_FOR_INCREASE = 5
//        private const val SUCCESS_CHUNK_SIZE_MULTIPLIER = 2
//        private const val FAILURE_CHUNK_SIZE_DIVIDER = 2
//
//        private const val BYTE_ARRAY_SIZE = 8 * 1024
//
//        private const val NETWORK_TIMEOUT_MS = 60_000L
//        private const val MAX_RETRIES = 5
//
//        private const val CHANNEL_ID = "image_upload_channel"
//        private const val CHANNEL_NAME = "Image Upload Channel"
//        private const val NOTIFICATION_ID = 1001
//    }
//
//    private val notificationManager =
//        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//    private var notificationSessionTitle: String = ""
//    private var notificationTotalImages: Int = 0
//    private var notificationCurrentImageIndex: Int = 0
//
//    private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
//
//    override suspend fun doWork(): WorkerResult {
//        createNotificationChannel()
//        val sessionIdStr = inputData.getString(KEY_SESSION_ID)
//        if (sessionIdStr == null) {
//            Log.e("ImageUploadWorker", "Session ID missing from worker input data.")
//            return WorkerResult.failure()
//        }
//
//        val sessionId = try {
//            UUID.fromString(sessionIdStr)
//        } catch (e: IllegalArgumentException) {
//            Log.e("ImageUploadWorker", "Invalid session ID format provided: $sessionIdStr", e)
//            return WorkerResult.failure()
//        }
//
//        val sessionWithSpecimens = sessionRepository.getSessionWithSpecimensById(sessionId)
//        if (sessionWithSpecimens == null) {
//            Log.e("ImageUploadWorker", "Session $sessionId not found in the database.")
//            return WorkerResult.failure()
//        }
//
//        val sessionDateStr = dateFormatter.format(Date(sessionWithSpecimens.session.createdAt))
//        notificationSessionTitle = "Session from $sessionDateStr"
//
//        val specimensToUpload = sessionWithSpecimens.specimens.filter {
//            it.imageUploadStatus != UploadStatus.COMPLETED
//        }
//
//        if (specimensToUpload.isEmpty()) {
//            Log.d("ImageUploadWorker", "No images to upload for session $sessionId.")
//            return WorkerResult.success()
//        }
//
//        setForeground(showInitialSessionNotification(specimensToUpload.size))
//
//        var successfulUploads = 0
//        var encounteredPermanentFailure = false
//
//        specimensToUpload.forEachIndexed { index, specimen ->
//            when (val result = uploadSingleSpecimen(specimen, sessionId, index + 1, specimensToUpload.size)) {
//                is DomainResult.Success -> {
//                    successfulUploads++
//            }
//                is DomainResult.Error -> {
//                    if (result.error !in listOf(
//                            NetworkError.REQUEST_TIMEOUT,
//                            NetworkError.NO_INTERNET,
//                            NetworkError.SERVER_ERROR
//                        )) {
//                        encounteredPermanentFailure = true
//                    }
//                }
//            }
//        }
//
//        showFinalStatusNotification(successfulUploads, specimensToUpload.size)
//        return if (successfulUploads == specimensToUpload.size) {
//            Log.i("ImageUploadWorker", "Work finished: All images uploaded successfully.")
//            WorkerResult.success()
//        } else if (encounteredPermanentFailure) {
//            Log.w("ImageUploadWorker", "Work finished: Permanent failure encountered. Not retrying.")
//            WorkerResult.failure()
//        } else {
//            Log.i("ImageUploadWorker", "Work finished: Transient failures encountered. Requesting retry.")
//            WorkerResult.retry()
//        }
//    }
//
//    private suspend fun uploadSingleSpecimen(
//        specimen: Specimen,
//        sessionId: UUID,
//        currentIndex: Int,
//        totalImages: Int
//    ): DomainResult<String, NetworkError> {
//        notificationTotalImages = totalImages
//        notificationCurrentImageIndex = currentIndex
//
//        val (file, contentType, md5) = try {
//            val (tempFile, type) = prepareFile(context.contentResolver, specimen.imageUri, specimen.id)
//            Triple(tempFile, type, calculateMD5(tempFile))
//        } catch (e: Exception) {
//            if (isStopped) {
//                Log.w("ImageUploadWorker", "Worker was stopped during file preparation.")
//                throw CancellationException("Worker was stopped during file preparation.", e)
//            }
//            Log.e("ImageUploadWorker", "Failed to prepare file or calculate MD5 for specimen ${specimen.id}.", e)
//            specimenRepository.updateSpecimen(specimen.copy(imageUploadStatus = UploadStatus.FAILED), sessionId)
//            return DomainResult.Error(NetworkError.CLIENT_ERROR)
//        }
//
//        val uploadResult = attemptUploadWithRetries(file, contentType, specimen, sessionId, md5)
//
//        val finalStatus = if (uploadResult is DomainResult.Success) {
//            UploadStatus.COMPLETED
//        } else {
//            UploadStatus.FAILED
//        }
//        specimenRepository.updateSpecimen(specimen.copy(imageUploadStatus = finalStatus), sessionId)
//        file.delete()
//        Log.d("ImageUploadWorker", "Cleaned up cache file: ${file.name}")
//
//        return uploadResult
//    }
//
//    private suspend fun attemptUploadWithRetries(
//        file: File,
//        contentType: String?,
//        specimen: Specimen,
//        sessionId: UUID,
//        md5: String
//    ): DomainResult<String, NetworkError> {
//        for (attempt in 1..MAX_RETRIES) {
//            try {
//                val result = performUpload(file, contentType, specimen, sessionId, md5)
//
//                if (result is DomainResult.Success) {
//                    Log.d("ImageUploadWorker", "Success for specimen ${specimen.id} on attempt $attempt")
//                    return result
//                }
//
//                result as DomainResult.Error<NetworkError>
//                Log.w(
//                    "ImageUploadWorker",
//                    "Failed attempt $attempt for specimen ${specimen.id} with error: ${result.error}"
//                )
//
//                val isRetryable = result.error in listOf(
//                    NetworkError.REQUEST_TIMEOUT,
//                    NetworkError.NO_INTERNET,
//                    NetworkError.SERVER_ERROR
//                )
//
//                if (!isRetryable || attempt == MAX_RETRIES) {
//                    if (!isRetryable) {
//                        Log.e("ImageUploadWorker", "Non-retryable error for specimen ${specimen.id}.")
//                    }
//                    return result
//                }
//
//            } catch (e: Exception) {
//                if (isStopped) {
//                    Log.w("ImageUploadWorker", "Worker was stopped by the system during attempt $attempt.")
//                    throw CancellationException("Worker was stopped by the system.", e)
//                }
//                Log.e("ImageUploadWorker", "Exception on attempt $attempt for specimen ${specimen.id}.", e)
//                return DomainResult.Error(NetworkError.UNKNOWN_ERROR)
//            }
//        }
//
//        return DomainResult.Error(NetworkError.UNKNOWN_ERROR)
//    }
//
//    private suspend fun performUpload(
//        file: File,
//        contentType: String?,
//        specimen: Specimen,
//        sessionId: UUID,
//        md5: String
//    ): DomainResult<String, NetworkError> {
//        val uniqueFingerprint = "${specimen.id}-$md5"
//        val tusPath = "specimens/${specimen.id}/images/tus"
//
//        tusClient.uploadCreationURL = URL(constructUrl(tusPath))
//
//        // TODO: UPDATE HERE WHEN WE GET REAL IMAGEID
//        val upload = createTusUpload(file, uniqueFingerprint, contentType, md5, "50")
//
//        Log.d("ImageUploadWorker", "Start/resume ${file.name} (fp=$uniqueFingerprint,md5=$md5)")
//
//        val uploaderResult = try {
//            val uploader = tusClient.resumeOrCreateUpload(upload)
//
//            Log.d("ImageUploadWorker", "Connection established for ${specimen.id}. Setting status to IN_PROGRESS.")
//            specimenRepository.updateSpecimen(
//                specimen.copy(imageUploadStatus = UploadStatus.IN_PROGRESS),
//                sessionId
//            )
//
//            DomainResult.Success(uploader)
//        } catch (e: TusProtocolException) {
//            return when {
//                e.causingConnection?.responseCode == HttpURLConnection.HTTP_CONFLICT -> {
//                    Log.w(
//                        "ImageUploadWorker",
//                        "resumeOrCreateUpload returned with 409 Conflict.",
//                        e
//                    )
//                    val location = e.causingConnection.getHeaderField("Location")
//                    if (location != null) {
//                        specimenRepository.updateSpecimen(
//                            specimen.copy(imageUploadStatus = UploadStatus.IN_PROGRESS),
//                            sessionId
//                        )
//                        DomainResult.Success(location)
//                    } else {
//                        Log.e(
//                            "ImageUploadWorker",
//                            "Conflict response received without a Location header."
//                        )
//                        DomainResult.Error(NetworkError.SERVER_ERROR)
//                    }
//                }
//
//                e.shouldRetry() -> {
//                    Log.w(
//                        "ImageUploadWorker",
//                        "resumeOrCreateUpload failed with a retryable server error.",
//                        e
//                    )
//                    DomainResult.Error(NetworkError.SERVER_ERROR)
//                }
//
//                else -> {
//                    Log.e(
//                        "ImageUploadWorker",
//                        "resumeOrCreateUpload failed with a non-retryable client error.",
//                        e
//                    )
//                    DomainResult.Error(NetworkError.CLIENT_ERROR)
//                }
//            }
//        } catch (e: IOException) {
//            Log.e("ImageUploadWorker", "resumeOrCreateUpload failed with IOException", e)
//            return DomainResult.Error(NetworkError.NO_INTERNET)
//        }
//
//        val uploader =
//            uploaderResult.successOrNull() ?: return DomainResult.Error(NetworkError.UNKNOWN_ERROR)
//
//        val loopResult = executeUploadLoop(uploader, upload)
//        if (loopResult is DomainResult.Error) {
//            return DomainResult.Error(loopResult.error)
//        }
//
//        return when (val finalUrlResult = safeFinish(uploader)) {
//            is DomainResult.Success -> {
//                updateProgressNotification("Verifying...")
//                Log.d("ImageUploadWorker", "Upload finished successfully: ${finalUrlResult.data}")
//                DomainResult.Success(finalUrlResult.data.toString())
//            }
//
//            is DomainResult.Error -> DomainResult.Error(finalUrlResult.error)
//        }
//    }
//
//    private suspend fun executeUploadLoop(
//        initialUploader: TusUploader,
//        upload: TusUpload
//    ): DomainResult<Unit, NetworkError> {
//        var uploader = initialUploader
//        var currentChunkSize = INITIAL_CHUNK_SIZE_BYTES
//        var successfulUploadsInARow = 0
//        var recoveryAttempts = 0
//
//        while (uploader.offset < upload.size) {
//            uploader.chunkSize = currentChunkSize
//            uploader.requestPayloadSize = currentChunkSize
//
//            val chunkResult = try {
//                withTimeout(NETWORK_TIMEOUT_MS) {
//                    DomainResult.Success(uploader.uploadChunk())
//                }
//            } catch (e: SocketTimeoutException) {
//                DomainResult.Error(NetworkError.REQUEST_TIMEOUT)
//            } catch (e: TimeoutCancellationException) {
//                DomainResult.Error(NetworkError.REQUEST_TIMEOUT)
//            } catch (e: TusProtocolException) {
//                if (e.shouldRetry()) {
//                    DomainResult.Error(NetworkError.SERVER_ERROR)
//                } else {
//                    DomainResult.Error(NetworkError.CLIENT_ERROR)
//                }
//            } catch (e: IOException) {
//                DomainResult.Error(NetworkError.NO_INTERNET)
//            } catch (e: Exception) {
//                if (isStopped) {
//                    Log.w("ImageUploadWorker", "Worker was stopped during chunk upload.")
//                    throw CancellationException("Worker was stopped during chunk upload.", e)
//                }
//                DomainResult.Error(NetworkError.UNKNOWN_ERROR)
//            }
//
//            when (chunkResult) {
//                is DomainResult.Success -> {
//                    val bytesUploaded = chunkResult.data
//                    if (bytesUploaded <= -1) {
//                        Log.w("ImageUploadWorker", "File stream ended unexpectedly.")
//                        break
//                    }
//                    val percent =
//                        if (upload.size > 0) (uploader.offset * 100 / upload.size).toInt() else 0
//                    updateProgressNotification("$percent%")
//                    setProgress(
//                        workDataOf(
//                            KEY_PROGRESS_UPLOADED to uploader.offset,
//                            KEY_PROGRESS_TOTAL to upload.size
//                        )
//                    )
//
//                    successfulUploadsInARow++
//                    recoveryAttempts = 0
//                    if (successfulUploadsInARow >= SUCCESS_STREAK_FOR_INCREASE) {
//                        currentChunkSize =
//                            (currentChunkSize * SUCCESS_CHUNK_SIZE_MULTIPLIER).coerceAtMost(
//                                MAX_CHUNK_SIZE_BYTES
//                            )
//                        Log.i(
//                            "ImageUploadWorker",
//                            "Increasing chunk size to $currentChunkSize bytes."
//                        )
//                        successfulUploadsInARow = 0
//                    }
//                }
//
//                is DomainResult.Error -> {
//                    val error = chunkResult.error
//                    successfulUploadsInARow = 0
//                    currentChunkSize =
//                        (currentChunkSize / FAILURE_CHUNK_SIZE_DIVIDER).coerceAtLeast(
//                            MIN_CHUNK_SIZE_BYTES
//                        )
//                    Log.w(
//                        "ImageUploadWorker",
//                        "Upload error: $error. Reducing chunk size to $currentChunkSize bytes."
//                    )
//
//                    if (error != NetworkError.REQUEST_TIMEOUT && error != NetworkError.NO_INTERNET && error != NetworkError.SERVER_ERROR) {
//                        return DomainResult.Error(error)
//                    }
//
//                    recoveryAttempts++
//
//                    if (recoveryAttempts >= MAX_RETRIES) {
//                        Log.e(
//                            "ImageUploadWorker",
//                            "Exceeded max recovery attempts for a single image upload. Failing."
//                        )
//                        return DomainResult.Error(error)
//                    }
//
//                    try {
//                        uploader = tusClient.resumeOrCreateUpload(upload)
//                    } catch (resumeException: Exception) {
//                        if (isStopped) {
//                            Log.w("ImageUploadWorker", "Worker was stopped by the system while trying to resume upload.")
//                            throw CancellationException("Worker was stopped by the system.", resumeException)
//                        }
//                        Log.e("ImageUploadWorker", "Failed to resume upload.", resumeException)
//                        return DomainResult.Error(NetworkError.UNKNOWN_ERROR)
//                    }
//                }
//            }
//        }
//        Log.d("ImageUploadWorker", "Upload loop finished. Final offset: ${uploader.offset}")
//        return DomainResult.Success(Unit)
//    }
//
//    private fun createTusUpload(
//        file: File,
//        fingerprint: String,
//        contentType: String?,
//        md5: String,
//        imageId: String
//    ) = TusUpload(file).apply {
//        this.fingerprint = fingerprint
//        metadata = mapOf(
//            "filename" to file.name,
//            "contentType" to (contentType ?: "application/octet-stream"),
//            "filemd5" to md5,
//            "imageId" to imageId
//        )
//    }
//
//    private fun calculateMD5(file: File): String {
//        val md = MessageDigest.getInstance("MD5")
//        FileInputStream(file).use { fis ->
//            val buffer = ByteArray(BYTE_ARRAY_SIZE)
//            var read: Int
//            while (fis.read(buffer).also { read = it } != -1) {
//                md.update(buffer, 0, read)
//            }
//        }
//        return md.digest().joinToString("") { "%02x".format(it) }
//    }
//
//    private suspend fun prepareFile(
//        resolver: ContentResolver,
//        source: Uri,
//        specimenId: String
//    ): Pair<File, String?> =
//        withContext(Dispatchers.IO) {
//            val mimeType = resolver.getType(source)
//            val extension = when (mimeType) {
//                "image/jpeg" -> "jpg"
//                "image/png" -> "png"
//                else -> "bin"
//            }
//            val filename = "upload_specimen_$specimenId.$extension"
//            val destination = File(context.cacheDir, filename)
//            if (destination.exists()) {
//                Log.d(
//                    "ImageUploadWorker",
//                    "Found existing cache file, reusing: ${destination.name}"
//                )
//            } else {
//                resolver.openInputStream(source)?.use { input ->
//                    FileOutputStream(destination).use { output ->
//                        input.copyTo(output)
//                    }
//                } ?: throw IOException("Unable to open $source")
//                Log.d(
//                    "ImageUploadWorker",
//                    "Prepared ${destination.name} (${destination.length()} bytes)"
//                )
//            }
//            return@withContext destination to mimeType
//        }
//
//    private suspend fun safeFinish(uploader: TusUploader): DomainResult<URL, NetworkError> =
//        withContext(Dispatchers.IO) {
//            try {
//                uploader.finish()
//                Log.d("ImageUploadWorker", "Tus finish() successful.")
//                DomainResult.Success(uploader.uploadURL)
//            } catch (e: TusProtocolException) {
//                Log.w("ImageUploadWorker", "finish() failed with TusProtocolException.", e)
//                if (e.shouldRetry()) {
//                    DomainResult.Error(NetworkError.SERVER_ERROR)
//                } else {
//                    DomainResult.Error(NetworkError.CLIENT_ERROR)
//                }
//            } catch (e: IOException) {
//                Log.e("ImageUploadWorker", "finish() failed due to IOException.", e)
//                DomainResult.Error(NetworkError.NO_INTERNET)
//            } catch (e: Exception) {
//                if (isStopped) {
//                    Log.w("ImageUploadWorker", "Worker was stopped during Tus finish().")
//                    throw CancellationException("Worker was stopped during Tus finish().", e)
//                }
//                Log.e("ImageUploadWorker", "finish() failed due to unexpected exception.", e)
//                DomainResult.Error(NetworkError.UNKNOWN_ERROR)
//            }
//        }
//
//    private fun createNotificationChannel() {
//        val channel =
//            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
//        notificationManager.createNotificationChannel(channel)
//    }
//
//    private fun showInitialSessionNotification(total: Int): ForegroundInfo {
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle(notificationSessionTitle)
//            .setContentText("Preparing to upload $total images…")
//            .setSmallIcon(R.drawable.ic_cloud_upload)
//            .setProgress(total, 0, true)
//            .setOngoing(true)
//            .build()
//        return ForegroundInfo(
//            NOTIFICATION_ID,
//            notification,
//            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
//        )
//    }
//
//    private fun updateProgressNotification(progressText: String) {
//        val filesCompleted = notificationCurrentImageIndex - 1
//
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle(notificationSessionTitle)
//            .setContentText("Uploading image $notificationCurrentImageIndex of $notificationTotalImages")
//            .setSubText(progressText)
//            .setSmallIcon(R.drawable.ic_cloud_upload)
//            .setProgress(notificationTotalImages, filesCompleted, false)
//            .setOngoing(true)
//            .build()
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
//
//    private fun showFinalStatusNotification(successful: Int, total: Int) {
//        val title = if (successful == total) "Upload complete" else "Upload error"
//        val message = "$notificationSessionTitle: $successful of $total images uploaded."
//        val icon = if (successful == total) R.drawable.ic_cloud_upload else R.drawable.ic_info
//
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setSmallIcon(icon)
//            .build()
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
//}
