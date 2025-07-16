package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import com.vci.vectorcamapp.core.domain.model.BoundingBox

@Composable
fun BoundingBoxOverlay(
    boundingBox: BoundingBox,
    overlaySize: IntSize,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val topLeft = Offset(
            x = boundingBox.topLeftX * overlaySize.width,
            y = boundingBox.topLeftY * overlaySize.height
        )
        val boxSize = Size(
            width = boundingBox.width * overlaySize.width,
            height = boundingBox.height * overlaySize.height
        )

        drawRect(
            color = if (boundingBox.confidence > 0.8) Color.Green else Color.Red,
            topLeft = topLeft,
            size = boxSize,
            style = Stroke(width = 4f)
        )
    }
}