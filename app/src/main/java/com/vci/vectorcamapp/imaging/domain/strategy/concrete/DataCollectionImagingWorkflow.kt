package com.vci.vectorcamapp.imaging.domain.strategy.concrete

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.imaging.domain.model.CapturedFrameProcessingResult
import com.vci.vectorcamapp.imaging.domain.model.LiveFrameProcessingResult
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
import com.vci.vectorcamapp.imaging.domain.strategy.ImagingWorkflow
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import javax.inject.Inject

class DataCollectionImagingWorkflow @Inject constructor(
    private val inferenceRepository: InferenceRepository,
) : ImagingWorkflow {
    override suspend fun processLiveFrame(bitmap: Bitmap): LiveFrameProcessingResult {
        return LiveFrameProcessingResult(
            specimenId = inferenceRepository.readSpecimenId(bitmap),
            previewInferenceResults = emptyList()
        )
    }

    override suspend fun processCapturedFrame(bitmap: Bitmap): Result<CapturedFrameProcessingResult, ImagingError> {
        return Result.Success(CapturedFrameProcessingResult())
    }

    override fun close() {
        inferenceRepository.closeResources()
    }
}