package com.vci.vectorcamapp.core.presentation.components.pill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun InfoPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .padding(MaterialTheme.dimensions.paddingSmall),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            iconPainter?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = "Pill Icon",
                    tint = color,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeSmall)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
