package com.vci.vectorcamapp.core.data.dto.specimen

import kotlinx.serialization.Serializable

@Serializable
data class SpecimenDto(
    val specimenId: String = "",
    val sessionId: Int = -1
)
