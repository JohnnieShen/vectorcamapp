package com.vci.vectorcamapp.imaging.presentation

import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.ui.geometry.Offset

sealed interface ImagingAction {
    data class CorrectSpecimenId(val specimenId: String) : ImagingAction
    data class ProcessFrame(val frame: ImageProxy) : ImagingAction
    data object SaveSessionProgress : ImagingAction
    data object SubmitSession : ImagingAction
    data class CaptureImage(val controller: LifecycleCameraController) : ImagingAction
    data object SaveImageToSession : ImagingAction
    data object RetakeImage : ImagingAction
    data class ManualFocusAt(val offset: Offset) : ImagingAction
    data object CancelManualFocus : ImagingAction
}
