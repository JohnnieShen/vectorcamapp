package com.vci.vectorcamapp.complete_session.details.presentation.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.vci.vectorcamapp.core.presentation.components.ui.InfoTile
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun CompleteSessionFormTile(
    title: String,
    iconPainter: Painter,
    iconDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {

    InfoTile(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
            modifier = Modifier.padding(MaterialTheme.dimensions.paddingLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall),
                modifier = Modifier.height(MaterialTheme.dimensions.componentHeightSmall)
            ) {
                VerticalDivider(
                    thickness = MaterialTheme.dimensions.dividerThickness,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.fillMaxHeight()
                )

                Icon(
                    painter = iconPainter,
                    contentDescription = iconDescription,
                    tint = MaterialTheme.colors.icon,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeMedium)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            content()
        }
    }
}
