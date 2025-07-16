package com.vci.vectorcamapp.imaging.presentation.components.specimen.gallery

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.imaging.presentation.components.camera.BoundingBoxOverlay
import com.vci.vectorcamapp.imaging.presentation.components.specimen.infocard.SpecimenInfoCard

@Composable
fun CapturedSpecimenOverlay(
    specimen: Specimen,
    boundingBox: BoundingBox,
    modifier: Modifier = Modifier,
    specimenBitmap: Bitmap? = null,
    onSpecimenIdCorrected: ((String) -> Unit)? = null,
    onRetakeImage: (() -> Unit)? = null,
    onSaveImageToSession: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val aspectRatio = 4f / 3f

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / aspectRatio)
            ) {
                val containerSize = IntSize(
                    width = with(density) { maxWidth.roundToPx() },
                    height = with(density) { maxHeight.roundToPx() }
                )

                if (specimenBitmap != null) {
                    Image(
                        bitmap = specimenBitmap.asImageBitmap(),
                        contentDescription = specimen.id,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (specimen.imageUri != Uri.EMPTY) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(specimen.imageUri).crossfade(true)
                            .build(),
                        contentDescription = specimen.id,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                BoundingBoxOverlay(
                    boundingBox = boundingBox, overlaySize = containerSize, modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SpecimenInfoCard(
                specimen = specimen,
                onSpecimenIdChanged = onSpecimenIdCorrected,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (onRetakeImage != null || onSaveImageToSession != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onRetakeImage?.let {
                        IconButton(
                            onClick = onRetakeImage,
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cancel),
                                contentDescription = "Retake Image",
                                tint = Color.White
                            )
                        }
                    }

                    onSaveImageToSession?.let {
                        IconButton(
                            onClick = onSaveImageToSession,
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "Add Image To Session",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
