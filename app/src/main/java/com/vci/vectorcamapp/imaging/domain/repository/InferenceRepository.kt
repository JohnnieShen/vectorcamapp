package com.vci.vectorcamapp.imaging.domain.repository

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.imaging.domain.enums.AbdomenStatusLabel
import com.vci.vectorcamapp.imaging.domain.enums.SexLabel
import com.vci.vectorcamapp.imaging.domain.enums.SpeciesLabel

interface InferenceRepository {
    suspend fun readSpecimenId(bitmap: Bitmap) : String
    suspend fun detectSpecimen(bitmap: Bitmap) : List<InferenceResult>
    suspend fun classifySpecimen(croppedBitmap: Bitmap) : Triple<SpeciesLabel?, SexLabel?, AbdomenStatusLabel?>
    fun closeResources()
}
