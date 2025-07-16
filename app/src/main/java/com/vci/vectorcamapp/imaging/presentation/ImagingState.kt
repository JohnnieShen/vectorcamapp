package com.vci.vectorcamapp.imaging.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenAndBoundingBox

data class ImagingState(
    val isCapturing: Boolean = false,
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
    val currentImageBytes: ByteArray? = null,
    val captureBoundingBox: BoundingBox? = null,
    val previewBoundingBoxes: List<BoundingBox> = emptyList(),
    val capturedSpecimensAndBoundingBoxes: List<SpecimenAndBoundingBox> = emptyList(),
    val displayOrientation: Int = 0,
    val manualFocusPoint: Offset? = null
)
