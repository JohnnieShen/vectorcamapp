package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import com.vci.vectorcamapp.core.domain.model.InferenceResult

@Composable
fun BoundingBoxOverlay(
    inferenceResult: InferenceResult,
    overlaySize: IntSize,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val topLeft = Offset(
            x = inferenceResult.bboxTopLeftX * overlaySize.width,
            y = inferenceResult.bboxTopLeftY * overlaySize.height
        )
        val boxSize = Size(
            width = inferenceResult.bboxWidth * overlaySize.width,
            height = inferenceResult.bboxHeight * overlaySize.height
        )

        drawRect(
            color = if (inferenceResult.bboxConfidence > 0.8) Color.Green else Color.Red,
            topLeft = topLeft,
            size = boxSize,
            style = Stroke(width = 4f)
        )
    }
}