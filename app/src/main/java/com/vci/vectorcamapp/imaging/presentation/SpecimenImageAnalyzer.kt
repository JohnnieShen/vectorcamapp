package com.vci.vectorcamapp.imaging.presentation

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class SpecimenImageAnalyzer(
    private val onFrameReady: (ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(frame: ImageProxy) {
        onFrameReady(frame)
    }
}
