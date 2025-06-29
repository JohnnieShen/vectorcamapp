package com.vci.vectorcamapp.imaging.presentation.extensions

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

fun ImageProxy.toUprightBitmap(displayOrientation: Int): Bitmap {
    val bitmap = this.toBitmap()

    val imageDegrees = this.imageInfo.rotationDegrees

    val displayDegrees = when (displayOrientation) {
        in 45..134 -> 90
        in 135..224 -> 180
        in 225..314 -> 270
        else -> 0
    }

    val totalDegrees = (imageDegrees - displayDegrees + 360) % 360

    val matrix = Matrix().apply {
        postRotate(totalDegrees.toFloat(), bitmap.width / 2f, bitmap.height / 2f)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
