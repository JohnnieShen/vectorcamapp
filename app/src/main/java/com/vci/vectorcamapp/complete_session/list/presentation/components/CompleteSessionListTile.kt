package com.vci.vectorcamapp.complete_session.list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Site
import com.vci.vectorcamapp.core.presentation.components.ui.ActionTile
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompleteSessionListTile(
    session: Session, site: Site, onClick: () -> Unit, modifier: Modifier = Modifier
) {

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val dateTimeFormatter =
        remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }

    session.completedAt?.let { completedAt ->
        ActionTile(
            onClick = onClick, modifier = modifier,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                modifier = Modifier.padding(MaterialTheme.dimensions.paddingLarge)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Completed Session on\n${dateFormatter.format(completedAt)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colors.textPrimary
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(MaterialTheme.dimensions.componentHeightSmall)
                            .background(
                                color = MaterialTheme.colors.iconBackground, shape = CircleShape
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

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colors.pillBackground,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(MaterialTheme.dimensions.paddingSmall)
                ) {
                    Text(
                        text = "Session ID: ${session.localId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colors.pillText
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                modifier = Modifier.padding(MaterialTheme.dimensions.paddingLarge)
            ) {
                CompleteSessionListDetailRow(
                    iconPainter = painterResource(R.drawable.ic_person),
                    iconDescription = "Person",
                    text = "Collector: ${session.collectorName}, ${session.collectorTitle}",
                )

                CompleteSessionListDetailRow(
                    iconPainter = painterResource(R.drawable.ic_pin),
                    iconDescription = "Pin",
                    text = "District: ${site.district}",
                )

                CompleteSessionListDetailRow(
                    iconPainter = painterResource(R.drawable.ic_map),
                    iconDescription = "Map",
                    text = "Sub-County: ${site.subCounty}",
                )

                CompleteSessionListDetailRow(
                    iconPainter = painterResource(R.drawable.ic_navigation),
                    iconDescription = "Navigation",
                    text = "Parish: ${site.parish}",
                )

                CompleteSessionListDetailRow(
                    iconPainter = painterResource(R.drawable.ic_clipboard),
                    iconDescription = "Clipboard",
                    text = "Sentinel Site: ${site.sentinelSite}",
                )

                CompleteSessionListDetailRow(
                    iconPainter = painterResource(R.drawable.ic_house),
                    iconDescription = "House",
                    text = "House Number: ${session.houseNumber}",
                )
            }

            HorizontalDivider(color = MaterialTheme.colors.divider, thickness = MaterialTheme.dimensions.dividerThickness)

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall),
                modifier = Modifier.padding(MaterialTheme.dimensions.paddingLarge)
            ) {
                Text(
                    text = "Created At: ${dateTimeFormatter.format(session.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textSecondary
                )

                Text(
                    text = "Completed At: ${dateTimeFormatter.format(session.completedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textSecondary
                )
            }
        }
    }
}
