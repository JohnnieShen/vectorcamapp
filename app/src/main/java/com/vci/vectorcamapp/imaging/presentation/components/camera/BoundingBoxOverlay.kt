package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun BoundingBoxOverlay(
    inferenceResult: InferenceResult,
    overlaySize: IntSize,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { MaterialTheme.dimensions.borderThicknessThick.toPx() }

    // Extract colors in Composable context
    val highConfidenceColor = MaterialTheme.colors.successConfirm
    val lowConfidenceColor = MaterialTheme.colors.warning

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
            color = if (inferenceResult.bboxConfidence > 0.9f) highConfidenceColor else lowConfidenceColor,
            topLeft = topLeft,
            size = boxSize,
            style = Stroke(width = strokeWidth)
        )
    }
}