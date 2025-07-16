package com.vci.vectorcamapp.imaging.domain

import android.graphics.Bitmap
import java.io.Closeable

interface SpecimenClassifier : Closeable {
    fun getInputTensorShape() : Pair<Int, Int>
    fun getOutputTensorShape() : Int
    suspend fun classify(croppedBitmap: Bitmap): Int?
}
