package com.vci.vectorcamapp.imaging.presentation

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenAndInferenceResult

data class ImagingState(
    val isProcessing: Boolean = false,
    val currentSpecimen: Specimen = Specimen(
        id = "",
        species = null,
        sex = null,
        abdomenStatus = null,
        imageUri = Uri.EMPTY,
        metadataUploadStatus = UploadStatus.NOT_STARTED,
        imageUploadStatus = UploadStatus.NOT_STARTED,
        capturedAt = 0L,
        submittedAt = null
    ),
    val currentInferenceResult: InferenceResult = InferenceResult(
        bboxTopLeftX = 0f,
        bboxTopLeftY = 0f,
        bboxWidth = 0f,
        bboxHeight = 0f,
        bboxConfidence = 0f,
        bboxClassId = 0,
        speciesLogits = null,
        sexLogits = null,
        abdomenStatusLogits = null
    ),
    val currentImageBytes: ByteArray? = null,
    val previewInferenceResults: List<InferenceResult> = emptyList(),
    val capturedSpecimensAndInferenceResults: List<SpecimenAndInferenceResult> = emptyList(),
    val displayOrientation: Int = 0,
    val manualFocusPoint: Offset? = null,
    val isCameraReady: Boolean = false
)
