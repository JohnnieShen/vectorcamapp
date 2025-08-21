package com.vci.vectorcamapp.ui.extensions

import android.graphics.RectF
import android.graphics.BlurMaskFilter
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntSize

fun Modifier.customShadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
    spread: Dp = 0.dp
): Modifier = this.then(
    Modifier.drawBehind {
        drawCustomShadow(
            color = color,
            offsetX = offsetX,
            offsetY = offsetY,
            blurRadius = blurRadius,
            cornerRadius = cornerRadius,
            spread = spread
        )
    }
)

private fun DrawScope.drawCustomShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blurRadius: Dp,
    cornerRadius: Dp,
    spread: Dp
) {
    val shadowPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.TRANSPARENT
        setShadowLayer(
            blurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            color.toArgb()
        )
        style = android.graphics.Paint.Style.FILL
    }

    val spreadPx = spread.toPx()
    val left = 0f - spreadPx
    val top = 0f - spreadPx
    val right = size.width + spreadPx
    val bottom = size.height + spreadPx

    val shadowRect = RectF(left, top, right, bottom)

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            shadowRect,
            cornerRadius.toPx(),
            cornerRadius.toPx(),
            shadowPaint
        )
    }
}

fun Modifier.zoomPanGesture(
    containerSize: IntSize,
    minScale: Float = 1f,
    maxScale: Float = 5f
): Modifier = composed {
    val scale = remember { mutableFloatStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale.floatValue * zoomChange).coerceIn(minScale, maxScale)
        scale.floatValue = newScale

        val maxX = (containerSize.width * (newScale - 1f)) / 2
        val maxY = (containerSize.height * (newScale - 1f)) / 2
        val newOffset = offset.value + panChange
        offset.value = Offset(
            newOffset.x.coerceIn(-maxX, maxX),
            newOffset.y.coerceIn(-maxY, maxY)
        )
    }

    val canPan: (Offset) -> Boolean = { delta ->
        val maxX = (containerSize.width * (scale.floatValue - 1f)) / 2
        val maxY = (containerSize.height * (scale.floatValue - 1f)) / 2
        val atLeft = offset.value.x <= -maxX + 0.5f && delta.x < 0
        val atRight = offset.value.x >= maxX - 0.5f && delta.x > 0
        val atTop = offset.value.y <= -maxY + 0.5f && delta.y < 0
        val atBottom = offset.value.y >= maxY - 0.5f && delta.y > 0
        !(atLeft || atRight || atTop || atBottom)
    }

    this
        .transformable(
            state = state,
            canPan = canPan,
            lockRotationOnZoomPan = true
        )
        .graphicsLayer {
            scaleX = scale.floatValue
            scaleY = scale.floatValue
            translationX = offset.value.x
            translationY = offset.value.y
        }
}
