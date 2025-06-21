package com.vci.vectorcamapp.imaging.domain

import android.graphics.Bitmap
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import java.io.Closeable

interface SpecimenDetector : Closeable {
    fun getInputTensorShape(): Pair<Int, Int>
    fun getOutputTensorShape(): Pair<Int, Int>
    suspend fun detect(bitmap: Bitmap): List<BoundingBox>
}
