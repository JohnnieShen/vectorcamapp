package com.vci.vectorcamapp.imaging.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.imaging.domain.repository.CameraRepository
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraRepositoryImplementation @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraRepository {
    override suspend fun captureImage(controller: LifecycleCameraController): Result<ImageProxy, ImagingError> {
        return suspendCoroutine { continuation ->
            controller.takePicture(
                ContextCompat.getMainExecutor(context),
                object : OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        continuation.resume(Result.Success(image))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        continuation.resume(Result.Error(ImagingError.CAPTURE_ERROR))
                    }
                })
        }
    }

    override suspend fun saveImage(
        jpegBytes: ByteArray, filename: String, currentSession: Session
    ): Result<Uri, ImagingError> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val sessionTimestamp = dateFormat.format(Date(currentSession.createdAt))

        return withContext(Dispatchers.IO) {
            val appName = context.getString(R.string.app_name)
            val directory = "${Environment.DIRECTORY_DCIM}/$appName/$sessionTimestamp"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val uri = resolver.insert(collection, contentValues) ?: return@withContext Result.Error(
                ImagingError.SAVE_ERROR
            )

            try {
                val outputStream = resolver.openOutputStream(uri)
                if (outputStream == null) {
                    deleteSavedImage(uri)
                    return@withContext Result.Error(ImagingError.SAVE_ERROR)
                }

                val writeSuccess = outputStream.use {
                    it.write(jpegBytes)
                    true
                }

                if (!writeSuccess) {
                    deleteSavedImage(uri)
                    return@withContext Result.Error(ImagingError.SAVE_ERROR)
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.Success(uri)
            } catch (e: Exception) {
                deleteSavedImage(uri)
                Result.Error(ImagingError.SAVE_ERROR)
            }
        }
    }

    override suspend fun deleteSavedImage(uri: Uri) {
        val resolver = context.contentResolver
        resolver.delete(uri, null, null)
    }
}