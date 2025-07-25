package com.vci.vectorcamapp.imaging.presentation

import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.components.button.ActionButton
import com.vci.vectorcamapp.core.presentation.components.empty.EmptySpace
import com.vci.vectorcamapp.core.presentation.components.form.TextEntryField
import com.vci.vectorcamapp.core.presentation.components.tile.InfoTile
import com.vci.vectorcamapp.imaging.presentation.components.camera.LiveCameraPreview
import com.vci.vectorcamapp.imaging.presentation.components.specimen.CapturedSpecimenTile
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun ImagingScreen(
    state: ImagingState, onAction: (ImagingAction) -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzer = remember {
        SpecimenImageAnalyzer { frame ->
            onAction(ImagingAction.ProcessFrame(frame))
        }
    }

    val controller = remember(lifecycleOwner) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
            imageCaptureFlashMode = ImageCapture.FLASH_MODE_OFF
            imageCaptureResolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY).build()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0, pageCount = { state.specimensWithImagesAndInferenceResults.size + 1 })

    HorizontalPager(
        state = pagerState, modifier = modifier.fillMaxSize()
    ) { page ->
        when {
            page < state.specimensWithImagesAndInferenceResults.size -> {
                val infiniteTransition = rememberInfiniteTransition(label = "arrow_animation")
                val arrowOffsetX by infiniteTransition.animateFloat(
                    initialValue = with(density) { MaterialTheme.dimensions.spacingSmall.toPx() },
                    targetValue = with(density) { -(MaterialTheme.dimensions.spacingSmall.toPx()) },
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800), repeatMode = RepeatMode.Reverse
                    ),
                    label = "arrow_offset"
                )

                Column(
                    verticalArrangement = Arrangement.Center, modifier = modifier.fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (page > 0) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_left),
                                contentDescription = "Previous Icon",
                                tint = MaterialTheme.colors.icon,
                                modifier = Modifier
                                    .offset { IntOffset((-arrowOffsetX).toInt(), 0) }
                                    .size(MaterialTheme.dimensions.iconSizeLarge))
                        } else {
                            EmptySpace(
                                width = MaterialTheme.dimensions.iconSizeLarge,
                                height = MaterialTheme.dimensions.iconSizeLarge
                            )
                        }

                        Box(
                            modifier = Modifier.background(
                                color = MaterialTheme.colors.accent,
                                shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
                            )
                        ) {
                            Text(
                                text = "Specimen ${page + 1} of ${state.specimensWithImagesAndInferenceResults.size} in this Session",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colors.textPrimary,
                                modifier = Modifier.padding(MaterialTheme.dimensions.paddingMedium)
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "Next Icon",
                            tint = MaterialTheme.colors.icon,
                            modifier = Modifier
                                .offset { IntOffset(arrowOffsetX.toInt(), 0) }
                                .size(MaterialTheme.dimensions.iconSizeLarge))
                    }

                    LazyColumn {
                        items(state.specimensWithImagesAndInferenceResults[page].specimenImagesAndInferenceResults) { (specimenImage, inferenceResult) ->
                            CapturedSpecimenTile(
                                specimen = state.specimensWithImagesAndInferenceResults[page].specimen,
                                specimenImage = specimenImage,
                                inferenceResult = inferenceResult,
                            )
                        }
                    }
                }
            }

            state.currentImageBytes != null -> {
                val specimenBitmap = remember(state.currentImageBytes) {
                    BitmapFactory.decodeByteArray(
                        state.currentImageBytes, 0, state.currentImageBytes.size
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center, modifier = modifier.fillMaxSize()
                ) {
                    CapturedSpecimenTile(
                        specimen = state.currentSpecimen,
                        specimenImage = state.currentSpecimenImage,
                        inferenceResult = state.currentInferenceResult,
                        specimenBitmap = specimenBitmap,
                        onSpecimenIdCorrected = { onAction(ImagingAction.CorrectSpecimenId(it)) })

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.paddingMedium),
                        modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.paddingMedium)
                    ) {
                        ActionButton(
                            label = "Retake Image",
                            onClick = { onAction(ImagingAction.RetakeImage) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            label = "Save To Session",
                            onClick = { onAction(ImagingAction.SaveImageToSession) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier.fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingLarge),
                        modifier = Modifier.padding(
                            start = MaterialTheme.dimensions.paddingMedium,
                            end = MaterialTheme.dimensions.paddingMedium,
                            top = MaterialTheme.dimensions.paddingLarge
                        )
                    ) {
                        ActionButton(
                            label = "Save and Exit",
                            onClick = { onAction(ImagingAction.SaveSessionProgress) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            label = "Submit",
                            onClick = { onAction(ImagingAction.SubmitSession) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    InfoTile(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = MaterialTheme.dimensions.paddingMedium)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colors.accent,
                                    shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
                                )
                            ) {
                                Text(
                                    text = "Specimen ${page + 1} in this Session",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colors.textPrimary,
                                    modifier = Modifier.padding(MaterialTheme.dimensions.paddingMedium)
                                )
                            }

                            TextEntryField(
                                placeholder = "Specimen ID",
                                value = state.currentSpecimen.id,
                                onValueChange = { onAction(ImagingAction.CorrectSpecimenId(it)) },
                                singleLine = true,
                                modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.paddingMedium)
                            )

                            LiveCameraPreview(
                                controller = controller,
                                inferenceResults = state.previewInferenceResults,
                                manualFocusPoint = state.manualFocusPoint,
                                onEnableManualFocus = { onAction(ImagingAction.ManualFocusAt(it)) },
                                onCancelManualFocus = { onAction(ImagingAction.CancelManualFocus) },
                            )

                            ActionButton(
                                label = "Capture",
                                onClick = { onAction(ImagingAction.CaptureImage(controller)) },
                                iconPainter = painterResource(id = R.drawable.ic_camera),
                                enabled = !state.isProcessing,
                                modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.paddingMedium)
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun ImagingScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ImagingScreen(
                state = ImagingState(), onAction = { }, modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
