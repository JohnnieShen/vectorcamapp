package com.vci.vectorcamapp.core.data.dto.specimen

import kotlinx.serialization.Serializable

@Serializable
data class PostSpecimenResponseDto(
    val message: String = "",
    val specimen: SpecimenResponseDto = SpecimenResponseDto()
)
