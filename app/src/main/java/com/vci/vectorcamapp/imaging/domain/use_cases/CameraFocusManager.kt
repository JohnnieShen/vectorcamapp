package com.vci.vectorcamapp.imaging.domain.use_cases

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LifecycleOwner
import com.vci.vectorcamapp.core.domain.model.BoundingBox

interface CameraFocusManager {
    fun bind(lifecycleOwner: LifecycleOwner)

    fun focusAt(offsetPx: Offset)

    fun autoFocusOn(box: BoundingBox)

    fun cancelFocus()

    fun calculateFocusRingOffset(
        focusPx: Offset,
        overlayPx: IntSize,
        focusSizeDp: Dp,
        density: Density
    ): Pair<Dp, Dp>
}
