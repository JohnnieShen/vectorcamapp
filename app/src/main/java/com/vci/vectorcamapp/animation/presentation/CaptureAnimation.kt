package com.vci.vectorcamapp.animation.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import kotlin.math.min
import kotlin.math.sin

private const val DURATION_MS = 1600
private const val FADE_IN_MS = 250
private const val FADE_OUT_MS = 400

private const val PULSE_FREQ = 4.0
private const val PULSE_AMP = 0.10

@Composable
fun CaptureAnimation(
    modifier: Modifier = Modifier, isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(FADE_IN_MS)),
        exit = fadeOut(tween(FADE_OUT_MS))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "capture_transition")
        val t by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "scan_progress"
        )

        val scannerHeight = MaterialTheme.dimensions.scannerLineHeight
        val cornerLength = MaterialTheme.dimensions.cornerLength
        val cornerStroke = MaterialTheme.dimensions.cornerStroke
        val cornerColor = MaterialTheme.colors.animationCornerColor

        val density = LocalDensity.current
        val (lineHpx, cornerLenPx, strokePx) = remember(density) {
            with(density) {
                Triple(
                    scannerHeight.toPx(),
                    cornerLength.toPx(),
                    cornerStroke.toPx()
                )
            }
        }

        var containerHeightPx by remember { mutableIntStateOf(0) }

        Box(modifier
                .fillMaxSize()
                .onSizeChanged { containerHeightPx = it.height }
        ) {
            Box(Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colors.animationOverlayColor)
            )

            Box(Modifier
                    .matchParentSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val inset = min(w, h) * 0.15f
                        onDrawBehind {
                            drawCorners(cornerColor.copy(0.7f), w, h, inset, cornerLenPx, strokePx)
                        }
                    }
            )

            Box(Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.dimensions.scannerLineHeight)
                    .graphicsLayer {
                        translationY = (t * (containerHeightPx - lineHpx)).coerceAtLeast(0f)
                    }
                    .background(Brush.horizontalGradient(
                        listOf(Color.Transparent, MaterialTheme.colors.animationScanColor, Color.Transparent)
                    ))
            )

            val pulseAlpha = (sin(t * Math.PI * PULSE_FREQ) * PULSE_AMP + PULSE_AMP).toFloat()
            Box(Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = pulseAlpha }
                    .background(MaterialTheme.colors.animationScanColor)
            )

            Text(
                text = "Capturing… Hold Still",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(MaterialTheme.dimensions.paddingLarge)
            )
        }
    }
}

/** Cached corner drawing */
private fun DrawScope.drawCorners(
    color: Color, w: Float, h: Float, inset: Float, len: Float, stroke: Float
) {
    fun corner(x: Float, y: Float, sx: Float, sy: Float) {
        drawLine(color, Offset(x, y), Offset(x + sx * len, y), stroke)
        drawLine(color, Offset(x, y), Offset(x, y + sy * len), stroke)
    }
    corner(inset, inset, 1f, 1f)
    corner(w - inset, inset, -1f, 1f)
    corner(inset, h - inset, 1f, -1f)
    corner(w - inset, h - inset, -1f, -1f)
}
