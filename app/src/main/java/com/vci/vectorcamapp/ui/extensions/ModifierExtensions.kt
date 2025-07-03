package com.vci.vectorcamapp.ui.extensions

import android.graphics.RectF
import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

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
