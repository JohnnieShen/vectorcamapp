package com.vci.vectorcamapp.complete_session.details.presentation.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Site
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.presentation.components.pill.InfoPill
import com.vci.vectorcamapp.ui.extensions.colors
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompleteSessionForm(
    session: Session, site: Site, surveillanceForm: SurveillanceForm?, modifier: Modifier = Modifier
) {
    val dateTimeFormatter =
        remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    session.completedAt?.let {
        Column(modifier = modifier.fillMaxSize()) {
            CompleteSessionFormTile(
                title = "Session Status",
                iconPainter = painterResource(R.drawable.ic_cloud_upload),
                iconDescription = "Cloud Upload"
            ) {
                Text(
                    text = "Created At: ${dateTimeFormatter.format(session.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Completed At: ${dateTimeFormatter.format(session.completedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                InfoPill(text = "Session Type: ${session.type}", color = MaterialTheme.colors.info)
            }

            CompleteSessionFormTile(
                title = "General Information",
                iconPainter = painterResource(R.drawable.ic_info),
                iconDescription = "Information"
            ) {
                Text(
                    text = "Collector Name: ${session.collectorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Collector Title: ${session.collectorTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Collection Date: ${dateFormatter.format(session.collectionDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Collection Method: ${session.collectionMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Specimen Condition: ${session.specimenCondition}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            CompleteSessionFormTile(
                title = "Geographical Information",
                iconPainter = painterResource(R.drawable.ic_pin),
                iconDescription = "Pin"
            ) {
                Text(
                    text = "District: ${site.district}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Sub-County: ${site.subCounty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Parish: ${site.parish}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "Sentinel Site: ${site.sentinelSite}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )

                Text(
                    text = "House Number: ${session.houseNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            surveillanceForm?.let {
                CompleteSessionFormTile(
                    title = "Surveillance Form",
                    iconPainter = painterResource(R.drawable.ic_clipboard),
                    iconDescription = "Clipboard"
                ) {
                    Text(
                        text = "Number of People who Slept in the House: ${it.numPeopleSleptInHouse}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.textPrimary
                    )

                    Text(
                        text = "Was Indoor Residual Spray (IRS) Conducted: ${if (it.wasIrsConducted) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.textPrimary
                    )

                    it.monthsSinceIrs?.let { monthsSinceIrs ->
                        Text(
                            text = "Months Since IRS: $monthsSinceIrs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colors.textPrimary
                        )
                    }

                    Text(
                        text = "Number of Long Lasting Insecticide-coated Nets (LLINs) Available: ${it.numLlinsAvailable}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.textPrimary
                    )

                    it.llinType?.let { llinType ->
                        Text(
                            text = "LLIN Type: $llinType",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colors.textPrimary
                        )
                    }

                    it.llinBrand?.let { llinBrand ->
                        Text(
                            text = "LLIN Brand: $llinBrand",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colors.textPrimary
                        )
                    }

                    it.numPeopleSleptUnderLlin?.let { numPeopleSleptUnderLlin ->
                        Text(
                            text = "Number of People who Slept Under LLIN: $numPeopleSleptUnderLlin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colors.textPrimary
                        )
                    }
                }
            }

            if (session.notes.isNotEmpty()) {
                CompleteSessionFormTile(
                    title = "Additional Notes",
                    iconPainter = painterResource(R.drawable.ic_notes),
                    iconDescription = "Notes"
                ) {
                    Text(
                        text = "Notes:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.textPrimary
                    )

                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}
