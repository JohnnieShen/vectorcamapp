package com.vci.vectorcamapp.core.presentation.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun ActionButton(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
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
        Text(
            text = label,
            color = MaterialTheme.colors.buttonText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
