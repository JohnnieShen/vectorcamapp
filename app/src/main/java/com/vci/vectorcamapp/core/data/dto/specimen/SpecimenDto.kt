package com.vci.vectorcamapp.core.data.dto.specimen

import kotlinx.serialization.Serializable

@Serializable
data class SpecimenDto(
    val id: Int? = null,
    val specimenId: String = "",
    val sessionId: Int = -1
)
