package com.vci.vectorcamapp.imaging.presentation.components.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.imaging.presentation.model.BoundingBoxUi

@Composable
fun LiveCameraPreviewPage(
    controller: LifecycleCameraController,
    boundingBoxesUiList: List<BoundingBoxUi>,
    onImageCaptured: () -> Unit,
    onSaveSessionProgress: () -> Unit,
    onSubmitSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    this.controller = controller
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = modifier.fillMaxSize()
        )

        boundingBoxesUiList.map {
            BoundingBoxOverlay(it, modifier.fillMaxSize())
        }

        IconButton(
            onClick = onSaveSessionProgress,
            modifier = modifier
                .padding(24.dp)
                .size(64.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_save),
                contentDescription = "Save Session Progress",
                modifier = modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = onSubmitSession,
            modifier = modifier
                .padding(24.dp)
                .size(64.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_upload),
                contentDescription = "Submit Session",
                modifier = modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }

        IconButton(
            onClick = onImageCaptured,
            modifier = modifier
                .padding(bottom = 48.dp)
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .align(Alignment.BottomCenter)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "Capture Image",
                modifier = modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
