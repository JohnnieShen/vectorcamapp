package com.vci.vectorcamapp.imaging.domain.repository

import android.graphics.Bitmap
import com.vci.vectorcamapp.imaging.domain.enums.AbdomenStatusLabel
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.domain.enums.SexLabel
import com.vci.vectorcamapp.imaging.domain.enums.SpeciesLabel
import com.vci.vectorcamapp.imaging.presentation.model.BoundingBoxUi

interface InferenceRepository {
    suspend fun readSpecimenId(bitmap: Bitmap) : String
    suspend fun detectSpecimen(bitmap: Bitmap) : List<BoundingBox>
    suspend fun classifySpecimen(bitmap: Bitmap) : Triple<SpeciesLabel?, SexLabel?, AbdomenStatusLabel?>
    fun convertToBoundingBox(boundingBoxUi: BoundingBoxUi) : BoundingBox
    fun convertToBoundingBoxUi(boundingBox: BoundingBox) : BoundingBoxUi
    fun closeResources()
}
