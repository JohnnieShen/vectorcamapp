package com.vci.vectorcamapp.core.domain.model.composites

import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.SpecimenImage

data class SpecimenImageAndInferenceResult(
    val specimenImage: SpecimenImage,
    val inferenceResult: InferenceResult?
)
