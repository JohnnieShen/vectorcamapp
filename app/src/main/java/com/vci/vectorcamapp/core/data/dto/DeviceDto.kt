package com.vci.vectorcamapp.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    val id: Int = -1,
    val programId: Int = -1,
    val model: String = "",
    val registeredAt: Long = 0L,
) {
    fun isEmpty() = id == -1
}
