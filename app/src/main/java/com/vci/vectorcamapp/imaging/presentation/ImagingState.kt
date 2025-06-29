package com.vci.vectorcamapp.imaging.presentation

import android.graphics.Bitmap
import android.net.Uri
import com.vci.vectorcamapp.core.domain.model.Specimen
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
        capturedAt = 0L,
    ),
    val currentImage: Bitmap? = null,
    val captureBoundingBoxUi: BoundingBoxUi? = null,
    val previewBoundingBoxesUiList: List<BoundingBoxUi> = emptyList(),
    val capturedSpecimensAndBoundingBoxesUi: List<SpecimenAndBoundingBoxUi> = emptyList(),
    val displayOrientation: Int = 0
)
