package com.vci.vectorcamapp.core.data.dto.surveillance_form

import kotlinx.serialization.Serializable

@Serializable
data class PostSurveillanceFormResponseDto(
    val message: String = "",
    val form: SurveillanceFormDto = SurveillanceFormDto()
)
