package com.vci.vectorcamapp.core.data.dto.specimen_image

data class CheckImageExistsResponseDto(
    val message: String = "",
    val image: SpecimenImageDto = SpecimenImageDto()
)
