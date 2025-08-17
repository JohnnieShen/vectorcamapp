package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vci.vectorcamapp.animation.presentation.CaptureAnimation
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.imaging.data.camera.CameraFocusControllerImplementation
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun LiveCameraPreview(
    controller: LifecycleCameraController,
    inferenceResults: List<InferenceResult>,
    manualFocusPoint: Offset?,
    onEnableManualFocus: (Offset) -> Unit,
    onCancelManualFocus: () -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current

    val previewView = remember {
        PreviewView(context).apply {
            this.controller = controller
            this.scaleType = PreviewView.ScaleType.FIT_CENTER
            controller.bindToLifecycle(lifecycleOwner)
        }
    }

    val cameraFocusController = remember(previewView, controller) {
        CameraFocusControllerImplementation(
            previewView = previewView, controller = controller
        )
    }

    LaunchedEffect(inferenceResults, manualFocusPoint) {
        if (manualFocusPoint == null) {
            if (inferenceResults.isNotEmpty()) {
                cameraFocusController.autoFocusAt(inferenceResults.first())
            } else {
                cameraFocusController.cancelFocus()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / MaterialTheme.dimensions.aspectRatio)
    ) {
        val containerSize = IntSize(
            width = with(density) { maxWidth.roundToPx() },
            height = with(density) { maxHeight.roundToPx() })

        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        inferenceResults.map {
            BoundingBoxOverlay(
                inferenceResult = it,
                overlaySize = containerSize
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        cameraFocusController.manualFocusAt(offset)
                        onEnableManualFocus(offset)
                    }
                }
        ) {
            manualFocusPoint?.let { focusPoint ->
                if (containerSize != IntSize.Zero) {
                    AutofocusRingOverlay(
                        focusPoint = focusPoint, overlaySize = containerSize, onCancel = {
                            cameraFocusController.cancelFocus()
                            onCancelManualFocus()
                        })
                }
            }
        }

        CaptureAnimation(
            modifier = Modifier.fillMaxSize(),
            isVisible = isProcessing
        )
    }
}
