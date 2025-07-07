package com.vci.vectorcamapp.imaging.domain.use_cases

import com.vci.vectorcamapp.imaging.presentation.model.BoundingBoxUi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LifecycleOwner

interface CameraFocusManager {
    fun bind(lifecycleOwner: LifecycleOwner)

    fun focusAt(offsetPx: Offset)

    fun autoFocusOn(box: BoundingBoxUi)

    fun cancelFocus()

    fun calculateFocusRingOffset(
        focusPx: Offset,
        overlayPx: IntSize,
        focusSizeDp: Dp,
        density: Density
    ): Pair<Dp, Dp>
}
