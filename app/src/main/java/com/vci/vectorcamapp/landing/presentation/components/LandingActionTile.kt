package com.vci.vectorcamapp.landing.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.components.tile.ActionTile
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun LandingActionTile(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {

    ActionTile(
        onClick = onClick, modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimensions.paddingMedium)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(MaterialTheme.dimensions.componentHeightLarge)
                    .background(
                        color = MaterialTheme.colors.iconBackground,
                        shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
                    )
            ) {
                Icon(
                    painter = icon,
                    contentDescription = "Action Icon",
                    tint = MaterialTheme.colors.icon,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeLarge)
                )

                if (badgeCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(
                                x = MaterialTheme.dimensions.paddingExtraSmall,
                                y = -MaterialTheme.dimensions.paddingExtraSmall
                            ),
                        containerColor = MaterialTheme.colors.error
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = MaterialTheme.colors.buttonText
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingExtraSmall),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.dimensions.paddingSmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colors.textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(MaterialTheme.dimensions.componentHeightMedium)
                    .background(
                        color = MaterialTheme.colors.iconBackground,
                        shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = "Arrow Right",
                    tint = MaterialTheme.colors.icon,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeMedium)
                )
            }
        }
    }
}
