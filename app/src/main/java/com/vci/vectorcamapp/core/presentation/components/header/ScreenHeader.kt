package com.vci.vectorcamapp.core.presentation.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.screenHeightFraction

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    val headerHeightFraction = 0.25f

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeightFraction(headerHeightFraction))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colors.headerGradientTopLeft,
                                MaterialTheme.colors.headerGradientBottomRight
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite,
                        )
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = MaterialTheme.dimensions.paddingLarge,
                            vertical = MaterialTheme.dimensions.paddingExtraLarge
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        leadingIcon()

                        trailingIcon()
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.paddingSmall),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colors.textPrimary,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }

        content()
    }
}
