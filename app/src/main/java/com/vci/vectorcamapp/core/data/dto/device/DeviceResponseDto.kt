package com.vci.vectorcamapp.core.data.dto.device

import kotlinx.serialization.Serializable

@Serializable
data class DeviceResponseDto(
    val deviceId: Int = -1,
    val model: String = "",
    val registeredAt: Long = 0L,
    val submittedAt: Long = 0L,
    val programId: Int = -1
)
