package com.vci.vectorcamapp.imaging.presentation

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class SpecimenImageAnalyzer(
    private val onFrameReady: (ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {
    companion object {
        const val FRAME_SKIP_RATE = 15
    }

    private var frameCounter = 0

    override fun analyze(frame: ImageProxy) {
        if (frameCounter % FRAME_SKIP_RATE == 0) {
            onFrameReady(frame)
            frameCounter = 0
        } else {
            frame.close()
            frameCounter++
        }
    }
}
