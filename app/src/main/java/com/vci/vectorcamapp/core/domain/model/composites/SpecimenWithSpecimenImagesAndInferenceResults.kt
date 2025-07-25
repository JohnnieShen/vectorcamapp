package com.vci.vectorcamapp.core.domain.model.composites

import com.vci.vectorcamapp.core.domain.model.Specimen

data class SpecimenWithSpecimenImagesAndInferenceResults(
    val specimen: Specimen,
    val specimenImagesAndInferenceResults: List<SpecimenImageAndInferenceResult>
)
