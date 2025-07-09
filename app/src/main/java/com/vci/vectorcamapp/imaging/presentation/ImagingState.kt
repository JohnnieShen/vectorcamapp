package com.vci.vectorcamapp.imaging.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.UploadStatus
import com.vci.vectorcamapp.imaging.presentation.model.BoundingBoxUi
import com.vci.vectorcamapp.imaging.presentation.model.composites.SpecimenAndBoundingBoxUi

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
    val currentImage: Bitmap? = null,
    val captureBoundingBoxUi: BoundingBoxUi? = null,
    val previewBoundingBoxesUiList: List<BoundingBoxUi> = emptyList(),
    val capturedSpecimensAndBoundingBoxesUi: List<SpecimenAndBoundingBoxUi> = emptyList(),
    val displayOrientation: Int = 0,
    val manualFocusPoint: Offset? = null
)
