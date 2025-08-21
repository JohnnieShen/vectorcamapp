package com.vci.vectorcamapp.imaging.domain.repository

import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import com.vci.vectorcamapp.core.domain.util.Result

interface CameraRepository {
    suspend fun captureImage(controller: LifecycleCameraController) : Result<ImageProxy, ImagingError>
    suspend fun saveImage(jpegBytes: ByteArray, filename: String, currentSession: Session) : Result<Uri, ImagingError>
    suspend fun deleteSavedImage(uri: Uri)
}
