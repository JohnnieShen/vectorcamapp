package com.vci.vectorcamapp.core.data.dto.device

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceResponseDto(
    val message: String = "",
    val device: DeviceResponseDto = DeviceResponseDto()
)
