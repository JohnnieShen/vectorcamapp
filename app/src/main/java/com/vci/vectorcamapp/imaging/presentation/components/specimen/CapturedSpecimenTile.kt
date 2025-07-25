package com.vci.vectorcamapp.imaging.presentation.components.specimen

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.presentation.components.form.TextEntryField
import com.vci.vectorcamapp.core.presentation.components.pill.InfoPill
import com.vci.vectorcamapp.core.presentation.components.tile.InfoTile
import com.vci.vectorcamapp.imaging.presentation.components.camera.BoundingBoxOverlay
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.extensions.zoomPanGesture
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CapturedSpecimenTile(
    specimen: Specimen,
    specimenImage: SpecimenImage,
    inferenceResult: InferenceResult,
    modifier: Modifier = Modifier,
    specimenBitmap: Bitmap? = null,
    onSpecimenIdCorrected: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
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

            Box(
                modifier = Modifier
                    .zoomPanGesture(containerSize)
            ) {
                if (specimenBitmap != null) {
                    Image(
                        bitmap = specimenBitmap.asImageBitmap(),
                        contentDescription = specimen.id,
                        contentScale = ContentScale.FillBounds,
                    )
                } else if (specimenImage.imageUri != Uri.EMPTY) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(specimenImage.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = specimen.id,
                        contentScale = ContentScale.Fit
                    )
                }

                BoundingBoxOverlay(
                    inferenceResult = inferenceResult, overlaySize = containerSize
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
            modifier = Modifier.padding(MaterialTheme.dimensions.paddingLarge)
        ) {
            if (onSpecimenIdCorrected == null) {
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
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall)) {
                    TextEntryField(
                        label = "Specimen ID",
                        value = specimen.id,
                        onValueChange = onSpecimenIdCorrected,
                        singleLine = true,
                    )

                    InfoPill(
                        text = "Please ensure that the specimen ID is correct!",
                        color = MaterialTheme.colors.warning,
                        iconPainter = painterResource(R.drawable.ic_warning)
                    )
                }
            }

            Text(
                text = if (specimenImage.species != null) "Species: ${specimenImage.species}" else "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colors.textPrimary
            )

            Text(
                text = if (specimenImage.sex != null) "Sex: ${specimenImage.sex}" else "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colors.textPrimary
            )

            Text(
                text = if (specimenImage.abdomenStatus != null) "Abdomen Status: ${specimenImage.abdomenStatus}" else "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colors.textPrimary
            )
        }

        if (onSpecimenIdCorrected == null) {
            HorizontalDivider(
                color = MaterialTheme.colors.divider,
                thickness = MaterialTheme.dimensions.dividerThickness
            )

            Text(
                text = "Captured At: ${dateTimeFormatter.format(specimenImage.capturedAt)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colors.textPrimary,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.dimensions.paddingLarge,
                    vertical = MaterialTheme.dimensions.paddingMedium
                )
            )
        }
    }
}
