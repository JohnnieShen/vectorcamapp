package com.vci.vectorcamapp.imaging.domain.repository

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.model.InferenceResult

interface InferenceRepository {
    suspend fun readSpecimenId(bitmap: Bitmap) : String
    suspend fun detectSpecimen(bitmap: Bitmap) : List<InferenceResult>
    suspend fun classifySpecimen(croppedBitmap: Bitmap) : Triple<List<Float>?, List<Float>?, List<Float>?>
    fun closeResources()
}
