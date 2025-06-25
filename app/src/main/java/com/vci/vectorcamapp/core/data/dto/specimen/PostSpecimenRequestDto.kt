package com.vci.vectorcamapp.core.data.dto.specimen

import com.vci.vectorcamapp.core.data.dto.inference_result.InferenceResultDto
import kotlinx.serialization.Serializable

@Serializable
data class PostSpecimenRequestDto(
    val specimenId: String = "",
    val sessionId: Int = -1,
    val species: String = "",
    val sex: String = "",
    val abdomenStatus: String = "",
    val capturedAt: Long = 0L,
    val inferenceResult: InferenceResultDto = InferenceResultDto()
)
