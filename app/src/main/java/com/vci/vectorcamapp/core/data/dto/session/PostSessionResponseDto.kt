package com.vci.vectorcamapp.core.data.dto.session

import kotlinx.serialization.Serializable

@Serializable
data class PostSessionResponseDto(
    val message: String = "",
    val session: SessionResponseDto = SessionResponseDto(),
)
