package com.vci.vectorcamapp.complete_session.details.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.vci.vectorcamapp.complete_session.details.presentation.enums.CompleteSessionDetailsTab
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun SegmentedTabBar(
    tabs: List<CompleteSessionDetailsTab>,
    selectedTab: CompleteSessionDetailsTab,
    onTabSelected: (CompleteSessionDetailsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.dimensions.paddingLarge,
                vertical = MaterialTheme.dimensions.paddingSmall
            )
            .background(
                color = MaterialTheme.colors.segmentedTabBarInactive,
                shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
            )
            .border(
                width = MaterialTheme.dimensions.borderThicknessThin,
                color = MaterialTheme.colors.fieldBorder,
                shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium))
                        .background(
                            brush = if (isSelected) Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colors.segmentedTabBarActiveGradientLeft,
                                    MaterialTheme.colors.segmentedTabBarActiveGradientRight
                                )
                            ) else Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colors.segmentedTabBarInactive,
                                    MaterialTheme.colors.segmentedTabBarInactive
                                )
                            )
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = MaterialTheme.dimensions.paddingMedium)) {

                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colors.segmentedTabBarActiveText
                        else MaterialTheme.colors.segmentedTabBarInactiveText
                    )
                }
            }
        }
    }
}
