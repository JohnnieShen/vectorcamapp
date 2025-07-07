package com.vci.vectorcamapp.surveillance_form.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.customShadow
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun SectionCard(
    sectionTitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.dimensions.paddingMedium)
            .customShadow(
                color = MaterialTheme.colors.cardGlow.copy(alpha = 0.25f),
                offsetY = MaterialTheme.dimensions.shadowOffsetSmall,
                blurRadius = MaterialTheme.dimensions.shadowBlurMedium,
                cornerRadius = MaterialTheme.dimensions.cornerRadiusMedium,
            )
            .background(
                color = MaterialTheme.colors.cardBackground,
                shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
            )
            .padding(MaterialTheme.dimensions.paddingLarge)
    ) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))
        content()
    }
}