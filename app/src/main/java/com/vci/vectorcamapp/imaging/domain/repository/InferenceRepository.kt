package com.vci.vectorcamapp.imaging.domain.repository

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.domain.enums.AbdomenStatusLabel
import com.vci.vectorcamapp.imaging.domain.enums.SexLabel
import com.vci.vectorcamapp.imaging.domain.enums.SpeciesLabel

interface InferenceRepository {
    suspend fun readSpecimenId(bitmap: Bitmap) : String
    suspend fun detectSpecimen(bitmap: Bitmap) : List<BoundingBox>
    suspend fun classifySpecimen(croppedAndPaddedBitmap: Bitmap?) : Triple<SpeciesLabel?, SexLabel?, AbdomenStatusLabel?>
    fun closeResources()
}
