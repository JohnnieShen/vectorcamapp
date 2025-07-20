package com.vci.vectorcamapp.core.domain.model.composites

import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Specimen

data class SpecimenAndInferenceResult(
    val specimen: Specimen,
    val inferenceResult: InferenceResult
)
