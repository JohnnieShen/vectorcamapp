package com.vci.vectorcamapp.core.data.dto.specimen_image

import com.vci.vectorcamapp.core.data.dto.inference_result.InferenceResultDto
import kotlinx.serialization.Serializable

@Serializable
data class PostSpecimenImageRequestDto(
    val species: String? = null,
    val sex: String? = null,
    val abdomenStatus: String? = null,
    val capturedAt: Long = 0L,
    val inferenceResult: InferenceResultDto = InferenceResultDto()
)
