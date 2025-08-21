package com.vci.vectorcamapp.imaging.data

import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.gpu.GpuDelegateFactory

object GpuDelegateManager {

    @Volatile
    private var gpuDelegate: GpuDelegate? = null

    fun getDelegate(): GpuDelegate {
        return gpuDelegate ?: synchronized(this) {
            createDelegate().also { gpuDelegate = it }
        }
    }

    private fun createDelegate(): GpuDelegate {
        val options = GpuDelegateFactory.Options().apply {
            inferencePreference = GpuDelegateFactory.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER
            isPrecisionLossAllowed = false
        }
        return GpuDelegate(options)
    }

    fun close() {
        synchronized(this) {
            gpuDelegate?.close()
            gpuDelegate = null
        }
    }
}
