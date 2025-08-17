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
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CaptureAnimation(
    modifier: Modifier = Modifier, isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible, enter = fadeIn(tween(250)), exit = fadeOut(tween(400))
    ) {
        // 1) Time base (single float)
        val t by rememberInfiniteTransition(label = "cap").animateFloat(
            initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing), repeatMode = RepeatMode.Restart
            ), label = "scan"
        )

        // 2) Constants / cached brushes
        val scanColor = Color(0xFF00FF7F)
        val overlay = Color(0x80000000)           // 50% black
        val cornerColor = Color.White.copy(0.7f)
        val lineBrush = remember {
            Brush.horizontalGradient(
                listOf(Color.Transparent, scanColor, Color.Transparent)
            )
        }

        val density = LocalDensity.current
        val lineHpx = with(density) { 3.dp.toPx() }
        val cornerLen = with(density) { 24.dp.toPx() }
        val strokePx = with(density) { 2.dp.toPx() }

        var containerHeightPx by remember { mutableIntStateOf(0) }

        Box(
            modifier
                .fillMaxSize()
                .onSizeChanged { containerHeightPx = it.height } // used for translationY
        ) {
            // A) Static dim overlay
            Box(
                Modifier
                    .matchParentSize()
                    .background(overlay)
            )

            // B) Static corner frame (drawn once by the renderer)
            Box(
                Modifier
                    .matchParentSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val inset = min(w, h) * 0.15f
                        onDrawBehind { drawCorners(cornerColor, w, h, inset, cornerLen, strokePx) }
                    })

            // C) Scanning line (GPU translated, no repaint of background)
            Box(Modifier
                .fillMaxWidth()
                .height(3.dp)
                .graphicsLayer {
                    // translation in pixels; keep within bounds
                    translationY = (t * (containerHeightPx - lineHpx)).coerceAtLeast(0f)
                }
                .background(lineBrush))

            // D) Subtle pulse (GPU alpha only)
            val pulseAlpha = ((sin(t * Math.PI * 4) * 0.10) + 0.10).toFloat()
            Box(Modifier
                .matchParentSize()
                .graphicsLayer { alpha = pulseAlpha }
                .background(scanColor))

            Text(
                text = "Capturing… Hold Still",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
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
