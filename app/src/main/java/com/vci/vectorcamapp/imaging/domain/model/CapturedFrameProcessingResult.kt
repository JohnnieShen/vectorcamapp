package com.vci.vectorcamapp.imaging.domain.model

import com.vci.vectorcamapp.core.domain.model.InferenceResult

data class CapturedFrameProcessingResult(
    val species: String? = null,
    val sex: String? = null,
    val abdomenStatus: String? = null,
    val capturedInferenceResult: InferenceResult? = null
)
