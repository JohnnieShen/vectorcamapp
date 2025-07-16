package com.vci.vectorcamapp.imaging.data

import android.util.Log
import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.StreamState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LifecycleOwner
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.domain.use_cases.CameraFocusManager

class CameraFocusManagerImplementation (
    private val previewView: PreviewView,
    private val controller: LifecycleCameraController,
) : CameraFocusManager {

    override fun bind(lifecycleOwner: LifecycleOwner) {
        previewView.controller = controller
        controller.bindToLifecycle(lifecycleOwner)
    }

    private fun buildAction(x: Float, y: Float): FocusMeteringAction {
        val point = previewView.meteringPointFactory.createPoint(x, y)
        return FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .disableAutoCancel()
            .build()
    }


    override fun focusAt(offsetPx: Offset) {
        controller.cameraControl
            ?.startFocusAndMetering(buildAction(offsetPx.x, offsetPx.y))
            ?: Log.w("CameraFocusManager", "focusAt(): cameraControl not ready yet")
    }


    override fun autoFocusOn(box: BoundingBox) {
        if (previewView.previewStreamState.value == StreamState.STREAMING) {
            val focusX = (box.topLeftX + box.width / 2f) * previewView.width
            val focusY = (box.topLeftY + box.height / 2f) * previewView.height

            controller.cameraControl
                ?.startFocusAndMetering(buildAction(focusX, focusY))
                ?: Log.w("CameraFocusManager", "autoFocusOn(): cameraControl not ready yet")
        }
    }

    override fun cancelFocus() {
        controller.cameraControl
            ?.cancelFocusAndMetering()
            ?: Log.w("CameraFocusManager", "cancelFocus(): cameraControl not ready yet")
    }

    override fun calculateFocusRingOffset(
        focusPoint : Offset,
        overlaySize : IntSize,
        focusBoxSize: Dp,
        density: Density
    ): Pair<Dp, Dp> {
        return with(density) {
            val sizePx = focusBoxSize.toPx()
            val x0 = (focusPoint.x - sizePx/2f).coerceIn(0f, overlaySize.width - sizePx)
            val y0 = (focusPoint.y - sizePx/2f).coerceIn(0f, overlaySize.height - sizePx)
            x0.toDp() to y0.toDp()
        }
    }
}
