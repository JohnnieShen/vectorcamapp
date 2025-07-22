package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun AutofocusRingOverlay(
    focusPoint: Offset,
    overlaySize: IntSize,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val ringDiameter = with(density) { MaterialTheme.dimensions.componentHeightLarge.toPx() }

    val offsetX = (focusPoint.x - ringDiameter / 2f)
        .coerceIn(0f, overlaySize.width - ringDiameter)
    val offsetY = (focusPoint.y - ringDiameter / 2f)
        .coerceIn(0f, overlaySize.height - ringDiameter)

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .size(MaterialTheme.dimensions.componentHeightLarge)
            .border(
                width = MaterialTheme.dimensions.borderThicknessThick,
                color = MaterialTheme.colors.info,
                shape = CircleShape
            )
            .clickable(onClick = onCancel)
    )
}
