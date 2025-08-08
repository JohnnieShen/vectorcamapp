package com.vci.vectorcamapp.imaging.domain.strategy.concrete

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.imaging.domain.enums.AbdomenStatusLabel
import com.vci.vectorcamapp.imaging.domain.enums.SexLabel
import com.vci.vectorcamapp.imaging.domain.enums.SpeciesLabel
import com.vci.vectorcamapp.imaging.domain.model.CapturedFrameProcessingResult
import com.vci.vectorcamapp.imaging.domain.model.LiveFrameProcessingResult
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
import com.vci.vectorcamapp.imaging.domain.strategy.ImagingWorkflow
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import javax.inject.Inject

class SurveillanceImagingWorkflow @Inject constructor(
    private val inferenceRepository: InferenceRepository
) : ImagingWorkflow {
    override suspend fun processLiveFrame(bitmap: Bitmap): LiveFrameProcessingResult {
        return LiveFrameProcessingResult(
            specimenId = inferenceRepository.readSpecimenId(bitmap),
            previewInferenceResults = inferenceRepository.detectSpecimen(bitmap)
        )
    }

    override suspend fun processCapturedFrame(bitmap: Bitmap): Result<CapturedFrameProcessingResult, ImagingError> {
        val captureInferenceResults = inferenceRepository.detectSpecimen(bitmap)

        when (captureInferenceResults.size) {
            0 -> return Result.Error(ImagingError.NO_SPECIMEN_FOUND)
            1 -> {
                val captureInferenceResult = captureInferenceResults.first()
                val topLeftXFloat =
                    captureInferenceResult.bboxTopLeftX * bitmap.width
                val topLeftYFloat =
                    captureInferenceResult.bboxTopLeftY * bitmap.height
                val widthFloat = captureInferenceResult.bboxWidth * bitmap.width
                val heightFloat = captureInferenceResult.bboxHeight * bitmap.height

                val topLeftXAbsolute = topLeftXFloat.toInt()
                val topLeftYAbsolute = topLeftYFloat.toInt()
                val widthAbsolute =
                    (widthFloat + (topLeftXFloat - topLeftXAbsolute)).toInt()
                val heightAbsolute =
                    (heightFloat + (topLeftYFloat - topLeftYAbsolute)).toInt()

                val clampedTopLeftX =
                    topLeftXAbsolute.coerceIn(0, bitmap.width - 1)
                val clampedTopLeftY =
                    topLeftYAbsolute.coerceIn(0, bitmap.height - 1)
                val clampedWidth =
                    widthAbsolute.coerceIn(1, bitmap.width - clampedTopLeftX)
                val clampedHeight =
                    heightAbsolute.coerceIn(1, bitmap.height - clampedTopLeftY)

                if (clampedWidth > 0 && clampedHeight > 0) {
                    val croppedBitmap = Bitmap.createBitmap(
                        bitmap,
                        clampedTopLeftX,
                        clampedTopLeftY,
                        clampedWidth,
                        clampedHeight
                    )

                    var (speciesResult, sexResult, abdomenStatusResult) = inferenceRepository.classifySpecimen(croppedBitmap)

                    val speciesIndex = speciesResult?.logits?.let { logits -> logits.indexOf(logits.max()) }
                    var sexIndex = sexResult?.logits?.let { logits -> logits.indexOf(logits.max()) }
                    var abdomenStatusIndex = abdomenStatusResult?.logits?.let { logits -> logits.indexOf(logits.max()) }

                    if (speciesResult?.logits == null || speciesIndex == SpeciesLabel.NON_MOSQUITO.ordinal) {
                        sexResult = null
                        sexIndex = null
                    }
                    if (sexResult?.logits == null || sexIndex == SexLabel.MALE.ordinal) {
                        abdomenStatusResult = null
                        abdomenStatusIndex = null
                    }

                    return Result.Success(
                        CapturedFrameProcessingResult(
                            species = speciesIndex?.let { index -> SpeciesLabel.entries[index].label },
                            sex = sexIndex?.let { index -> SexLabel.entries[index].label },
                            abdomenStatus = abdomenStatusIndex?.let { index -> AbdomenStatusLabel.entries[index].label },
                            capturedInferenceResult = InferenceResult(
                                bboxTopLeftX = captureInferenceResult.bboxTopLeftX,
                                bboxTopLeftY = captureInferenceResult.bboxTopLeftY,
                                bboxWidth = captureInferenceResult.bboxWidth,
                                bboxHeight = captureInferenceResult.bboxHeight,
                                bboxConfidence = captureInferenceResult.bboxConfidence,
                                bboxClassId = captureInferenceResult.bboxClassId,
                                speciesLogits = speciesResult?.logits,
                                sexLogits = sexResult?.logits,
                                abdomenStatusLogits = abdomenStatusResult?.logits,
                                speciesInferenceDuration = speciesResult?.inferenceDuration,
                                sexInferenceDuration = sexResult?.inferenceDuration,
                                abdomenStatusInferenceDuration = abdomenStatusResult?.inferenceDuration,
                            )
                        )
                    )

                } else {
                    return Result.Error(ImagingError.PROCESSING_ERROR)
                }
            }

            else -> return Result.Error(ImagingError.MULTIPLE_SPECIMENS_FOUND)
        }
    }

    override fun close() {
        inferenceRepository.closeResources()
    }
}