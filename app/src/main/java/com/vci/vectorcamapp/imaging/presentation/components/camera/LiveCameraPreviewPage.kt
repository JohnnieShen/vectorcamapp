package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.data.camera.CameraFocusControllerImplementation
import com.vci.vectorcamapp.imaging.presentation.ImagingAction

@Composable
fun LiveCameraPreviewPage(
    controller: LifecycleCameraController,
    boundingBoxes: List<BoundingBox>,
    manualFocusPoint: Offset?,
    onImageCaptured: () -> Unit,
    onSaveSessionProgress: () -> Unit,
    onSubmitSession: () -> Unit,
    onAction: (ImagingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val aspectRatio = 4f / 3f

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

    LaunchedEffect(boundingBoxes, manualFocusPoint) {
        if (manualFocusPoint == null) {
            if (boundingBoxes.isNotEmpty()) {
                cameraFocusController.autoFocusAt(boundingBoxes.first())
            } else {
                cameraFocusController.cancelFocus()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().aspectRatio(1f / aspectRatio)
        ) {
            val containerSize = IntSize(
                width = with(density) { maxWidth.roundToPx() },
                height = with(density) { maxHeight.roundToPx() })

            AndroidView(
                factory = {
                    previewView
                }, modifier = Modifier.fillMaxSize()
            )

            boundingBoxes.map {
                BoundingBoxOverlay(it, overlaySize = containerSize, Modifier.fillMaxSize())
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            cameraFocusController.manualFocusAt(offset)
                            onAction(ImagingAction.ManualFocusAt(offset))
                        }
                    }) {
                manualFocusPoint?.let { focusPoint ->
                    if (containerSize != IntSize.Zero) {
                        AutofocusRingOverlay(
                            focusPoint = focusPoint, overlaySize = containerSize, onCancel = {
                                cameraFocusController.cancelFocus()
                                onAction(ImagingAction.CancelManualFocus)
                            })
                    }
                }
            }
        }

        IconButton(
            onClick = onSaveSessionProgress,
            modifier = Modifier
                .padding(24.dp)
                .size(64.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_save),
                contentDescription = "Save Session Progress",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = onSubmitSession,
            modifier = Modifier
                .padding(24.dp)
                .size(64.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cloud_upload),
                contentDescription = "Submit Session",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = onImageCaptured,
            modifier = Modifier
                .padding(bottom = 48.dp)
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .align(Alignment.BottomCenter)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "Capture Image",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
