package com.vci.vectorcamapp.imaging.presentation

import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vci.vectorcamapp.imaging.presentation.components.camera.LiveCameraPreviewPage
import com.vci.vectorcamapp.imaging.presentation.components.specimen.gallery.CapturedSpecimenOverlay
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun ImagingScreen(
    state: ImagingState, onAction: (ImagingAction) -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                .build()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { state.capturedSpecimensAndInferenceResults.size + 1 }
    )

    HorizontalPager(
        state = pagerState, modifier = modifier.fillMaxSize()
    ) { page ->
        when {
            page < state.capturedSpecimensAndInferenceResults.size -> {
                CapturedSpecimenOverlay(
                    specimen = state.capturedSpecimensAndInferenceResults[page].specimen,
                    inferenceResult = state.capturedSpecimensAndInferenceResults[page].inferenceResult,
                    modifier = modifier
                )
            }

            state.currentImageBytes != null -> {
                val specimenBitmap = remember(state.currentImageBytes) {
                    BitmapFactory.decodeByteArray(state.currentImageBytes, 0, state.currentImageBytes.size)
                }

                CapturedSpecimenOverlay(
                    specimen = state.currentSpecimen,
                    inferenceResult = state.currentInferenceResult,
                    modifier = modifier,
                    specimenBitmap = specimenBitmap,
                    onSpecimenIdCorrected = { onAction(ImagingAction.CorrectSpecimenId(it)) },
                    onRetakeImage = { onAction(ImagingAction.RetakeImage) },
                    onSaveImageToSession = { onAction(ImagingAction.SaveImageToSession) }
                )
            }

            else -> {
                LiveCameraPreviewPage(
                    controller = controller,
                    inferenceResults = state.previewInferenceResults,
                    onImageCaptured = {
                        onAction(ImagingAction.CaptureImage(controller))
                    },
                    onSaveSessionProgress = { onAction(ImagingAction.SaveSessionProgress) },
                    onSubmitSession = { onAction(ImagingAction.SubmitSession) },
                    manualFocusPoint = state.manualFocusPoint,
                    onAction = onAction,
                    modifier = modifier,
                    captureEnabled = !state.isProcessing
                )
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
