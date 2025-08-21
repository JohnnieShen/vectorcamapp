package com.vci.vectorcamapp.complete_session.details.presentation.components.specimens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.presentation.components.tile.InfoTile
import com.vci.vectorcamapp.ui.extensions.color
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.extensions.displayText
import java.text.SimpleDateFormat
import java.util.Locale
import com.vci.vectorcamapp.ui.extensions.zoomPanGesture

@Composable
fun CompleteSessionSpecimensTile(
    session: Session,
    specimen: Specimen,
    specimenImage: SpecimenImage,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
) {

    val context = LocalContext.current
    var density = LocalDensity.current
    val dateTimeFormatter =
        remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }

    InfoTile(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / MaterialTheme.dimensions.aspectRatio)
                .clip(RectangleShape)
        ) {
            val containerSize = IntSize(
                width = with(density) { maxWidth.roundToPx() },
                height = with(density) { maxHeight.roundToPx() }
            )

            AsyncImage(
                model = ImageRequest.Builder(context).data(specimenImage.imageUri).build(),
                contentDescription = "Specimen Image: ${specimen.id}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomPanGesture(containerSize)
            )

            badgeText?.let {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(MaterialTheme.dimensions.paddingSmall),
                    containerColor = MaterialTheme.colors.info,
                    contentColor = MaterialTheme.colors.buttonText
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(MaterialTheme.dimensions.paddingSmall)
                    )
                }
            }
        }
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

            Text(
                text = buildAnnotatedString {
                    append("Metadata Upload Status: ")
                    withStyle(SpanStyle(color = specimenImage.metadataUploadStatus.color())) {
                        append(specimenImage.metadataUploadStatus.displayText(context))
                    }
                },
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = buildAnnotatedString {
                    append("Image Upload Status: ")
                    withStyle(SpanStyle(color = specimenImage.imageUploadStatus.color())) {
                        append(specimenImage.imageUploadStatus.displayText(context))
                    }
                },
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = if (specimenImage.species != null) "Species: ${specimenImage.species}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.textPrimary
            )

            Text(
                text = if (specimenImage.sex != null) "Sex: ${specimenImage.sex}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.textPrimary
            )

            Text(
                text = if (specimenImage.abdomenStatus != null) "Abdomen Status: ${specimenImage.abdomenStatus}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.textPrimary
            )

            Text(
                text = "Captured At: ${dateTimeFormatter.format(session.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.textPrimary
            )
        }
    }
}

