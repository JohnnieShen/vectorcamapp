package com.vci.vectorcamapp.core.data.dto.specimen_image

import com.vci.vectorcamapp.core.data.dto.inference_result.InferenceResultDto

data class SpecimenImageDto(
    val id: Int = -1,
    val url: String = "",
    val species: String? = null,
    val sex: String? = null,
    val abdomenStatus: String? = null,
    val capturedAt: Long = 0L,
    val submittedAt: Long? = null,
    val inferenceResult: InferenceResultDto = InferenceResultDto(),
    val filemd5: String = "",
)
