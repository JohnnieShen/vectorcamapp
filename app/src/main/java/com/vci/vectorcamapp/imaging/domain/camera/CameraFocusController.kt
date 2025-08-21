package com.vci.vectorcamapp.imaging.domain.camera

import androidx.compose.ui.geometry.Offset
import com.vci.vectorcamapp.core.domain.model.InferenceResult

interface CameraFocusController {
    fun manualFocusAt(offset: Offset)
    fun autoFocusAt(inferenceResult: InferenceResult)
    fun cancelFocus()
}
