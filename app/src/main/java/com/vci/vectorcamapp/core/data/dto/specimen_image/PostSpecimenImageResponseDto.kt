package com.vci.vectorcamapp.core.data.dto.specimen_image

import kotlinx.serialization.Serializable

@Serializable
data class PostSpecimenImageResponseDto(
    val message: String = "",
    val image: SpecimenImageDto = SpecimenImageDto()
)
