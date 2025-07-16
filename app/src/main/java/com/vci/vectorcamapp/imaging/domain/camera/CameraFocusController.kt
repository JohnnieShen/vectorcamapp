package com.vci.vectorcamapp.imaging.domain.camera

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LifecycleOwner
import com.vci.vectorcamapp.core.domain.model.BoundingBox

interface CameraFocusController {
    fun bind(lifecycleOwner: LifecycleOwner)
    fun manualFocusAt(offsetPx: Offset)
    fun autoFocusAt(box: BoundingBox)
    fun cancelFocus()
}
