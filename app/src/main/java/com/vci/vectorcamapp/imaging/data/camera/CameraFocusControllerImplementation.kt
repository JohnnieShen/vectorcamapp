package com.vci.vectorcamapp.imaging.data.camera

import android.util.Log
import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.StreamState
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.domain.camera.CameraFocusController

class CameraFocusControllerImplementation (
    private val previewView: PreviewView,
    private val controller: LifecycleCameraController,
) : CameraFocusController {

    override fun bind(lifecycleOwner: LifecycleOwner) {
        previewView.controller = controller
        controller.bindToLifecycle(lifecycleOwner)
    }

    override fun manualFocusAt(offsetPx: Offset) {
        controller.cameraControl
            ?.startFocusAndMetering(buildFocusAction(offsetPx.x, offsetPx.y))
            ?: Log.w("CameraFocusManager", "focusAt(): cameraControl not ready yet")
    }

    override fun autoFocusAt(box: BoundingBox) {
        if (previewView.previewStreamState.value == StreamState.STREAMING) {
            val focusX = (box.topLeftX + box.width / 2f) * previewView.width
            val focusY = (box.topLeftY + box.height / 2f) * previewView.height

            controller.cameraControl
                ?.startFocusAndMetering(buildFocusAction(focusX, focusY))
                ?: Log.w("CameraFocusManager", "autoFocusOn(): cameraControl not ready yet")
        }
    }

    private fun buildFocusAction(x: Float, y: Float): FocusMeteringAction {
        val point = previewView.meteringPointFactory.createPoint(x, y)
        return FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .disableAutoCancel()
            .build()
    }

    override fun cancelFocus() {
        controller.cameraControl
            ?.cancelFocusAndMetering()
            ?: Log.w("CameraFocusManager", "cancelFocus(): cameraControl not ready yet")
    }
}
