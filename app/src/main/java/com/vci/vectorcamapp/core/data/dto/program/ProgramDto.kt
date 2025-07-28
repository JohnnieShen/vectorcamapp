package com.vci.vectorcamapp.core.data.dto.program

import kotlinx.serialization.Serializable

@Serializable
data class ProgramDto(
    val id: Int,
    val name: String,
    val country: String
)