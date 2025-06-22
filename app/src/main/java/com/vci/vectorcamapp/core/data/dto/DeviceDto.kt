package com.vci.vectorcamapp.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    val id: Int? = null,
    val programId: Int = -1,
    val model: String = "",
    val registeredAt: Long = 0L,
) {
    fun hasId() = id != null
}
