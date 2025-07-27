package com.vci.vectorcamapp.core.data.dto.specimen

import kotlinx.serialization.Serializable

@Serializable
data class PostSpecimenRequestDto(
    val specimenId: String = ""
)
