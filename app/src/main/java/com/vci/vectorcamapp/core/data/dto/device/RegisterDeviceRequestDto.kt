package com.vci.vectorcamapp.core.data.dto.device

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequestDto(
    val model: String = "",
    val registeredAt: Long = 0L,
    val programId: Int = -1
)
