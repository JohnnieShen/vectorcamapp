package com.vci.vectorcamapp.complete_session.details.presentation.components.specimens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.presentation.components.tile.InfoTile
import com.vci.vectorcamapp.ui.extensions.color
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.extensions.displayName
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompleteSessionSpecimensTile(
    session: Session, specimen: Specimen, modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val dateTimeFormatter =
        remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }

    InfoTile(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(specimen.imageUri).build(),
            contentDescription = "Specimen Image: ${specimen.id}",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

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
                    painter = painterResource(R.drawable.ic_specimen),
                    contentDescription = "Mosquito",
                    tint = MaterialTheme.colors.icon,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeMedium)
                )

                Text(
                    text = "Specimen ID: ${specimen.id}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            specimen.metadataUploadStatus.let { status ->
                Text(
                    text = buildAnnotatedString {
                        append("Metadata Upload Status: ")
                        withStyle(SpanStyle(color = status.color())) {
                            append(status.displayName())
                        }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            specimen.imageUploadStatus.let { status ->
                Text(
                    text = buildAnnotatedString {
                        append("Image Upload Status: ")
                        withStyle(SpanStyle(color = status.color())) {
                            append(status.displayName())
                        }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            specimen.species?.let { species ->
                Text(
                    text = "Species: $species",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            specimen.sex?.let { sex ->
                Text(
                    text = "Sex: $sex",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            specimen.abdomenStatus?.let { abdomenStatus ->
                Text(
                    text = "Abdomen Status: $abdomenStatus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.textPrimary
                )
            }

            Text(
                text = "Created At: ${dateTimeFormatter.format(session.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.textPrimary
            )
        }
    }
}
