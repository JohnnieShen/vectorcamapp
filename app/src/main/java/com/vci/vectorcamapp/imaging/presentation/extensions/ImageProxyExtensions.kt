package com.vci.vectorcamapp.imaging.presentation.extensions

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

fun ImageProxy.toUprightBitmap(): Bitmap {
    val bitmap = this.toBitmap()
    val matrix = Matrix().apply {
        postRotate(90f, bitmap.width / 2f, bitmap.height / 2f)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
