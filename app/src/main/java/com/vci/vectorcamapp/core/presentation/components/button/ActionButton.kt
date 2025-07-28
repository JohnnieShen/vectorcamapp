package com.vci.vectorcamapp.core.presentation.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    textSize: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colors.transparent,
            disabledContainerColor = MaterialTheme.colors.disabled
        ),
        shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium),
        modifier = modifier
            .height(MaterialTheme.dimensions.componentHeightMedium)
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colors.buttonGradientLeft,
                        MaterialTheme.colors.buttonGradientRight
                    )
                ), shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
            )
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium), verticalAlignment = Alignment.CenterVertically) {
            iconPainter?.let {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = MaterialTheme.colors.buttonText,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeMedium)
                )
            }

            Text(
                text = label,
                color = MaterialTheme.colors.buttonText,
                style = textSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
