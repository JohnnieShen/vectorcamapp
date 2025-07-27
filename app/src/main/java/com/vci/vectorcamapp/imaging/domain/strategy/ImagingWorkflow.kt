package com.vci.vectorcamapp.imaging.domain.strategy

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.imaging.domain.model.CapturedFrameProcessingResult
import com.vci.vectorcamapp.imaging.domain.model.LiveFrameProcessingResult
import com.vci.vectorcamapp.imaging.domain.util.ImagingError

interface ImagingWorkflow {
    suspend fun processLiveFrame(bitmap: Bitmap): LiveFrameProcessingResult
    suspend fun processCapturedFrame(bitmap: Bitmap): Result<CapturedFrameProcessingResult, ImagingError>
    fun close()
}
