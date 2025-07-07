package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.imaging.data.CameraFocusManagerImplementation
import com.vci.vectorcamapp.imaging.presentation.ImagingAction
import com.vci.vectorcamapp.imaging.presentation.model.BoundingBoxUi

@Composable
fun LiveCameraPreviewPage(
    controller: LifecycleCameraController,
    boundingBoxesUiList: List<BoundingBoxUi>,
    manualFocusPoint: Offset?,
    onImageCaptured: () -> Unit,
    onSaveSessionProgress: () -> Unit,
    onSubmitSession: () -> Unit,
    onAction: (ImagingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val density = LocalDensity.current

    val cameraManager = remember(previewView, controller) {
        CameraFocusManagerImplementation(
            previewView = previewView,
            controller = controller
        )
    }

    val focusBoxSize = 64.dp
    val aspectRatio = 4f / 3f
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    DisposableEffect(lifecycleOwner) {
        cameraManager.bind(lifecycleOwner)
        onDispose {
            cameraManager.cancelFocus()
        }
    }

    LaunchedEffect(boundingBoxesUiList, manualFocusPoint) {
        if (manualFocusPoint == null) {
            if (boundingBoxesUiList.isNotEmpty()) {
                cameraManager.autoFocusOn(boundingBoxesUiList.first())
            } else {
                cameraManager.cancelFocus()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / aspectRatio)
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { overlaySize = it }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            cameraManager.focusAt(offset)
                            onAction(ImagingAction.ManualFocusAt(offset))
                        }
                    }
            ) {
                manualFocusPoint?.let { focusPoint ->
                    if (overlaySize != IntSize.Zero) {
                        val (offsetX, offsetY) = cameraManager.calculateFocusRingOffset(
                            focusPoint = focusPoint,
                            overlaySize = overlaySize,
                            focusBoxSize = focusBoxSize,
                            density = density
                        )

                        Box(
                            modifier = Modifier
                                .offset(x = offsetX, y = offsetY)
                                .size(focusBoxSize)
                                .border(2.dp, Color.Cyan, CircleShape)
                                .clickable {
                                    cameraManager.cancelFocus()
                                    onAction(ImagingAction.CancelManualFocus)
                                }
                        )
                    }
                }
            }
        }

        boundingBoxesUiList.forEach {
            BoundingBoxOverlay(it, Modifier.fillMaxSize())
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
                painter = painterResource(id = R.drawable.ic_upload),
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
