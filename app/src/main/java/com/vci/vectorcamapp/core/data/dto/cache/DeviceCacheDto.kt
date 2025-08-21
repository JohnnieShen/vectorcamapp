package com.vci.vectorcamapp.core.data.dto.cache

import kotlinx.serialization.Serializable

@Serializable
data class DeviceCacheDto(
    val id: Int = -1,
    val programId: Int = -1,
    val model: String = "",
    val registeredAt: Long = 0L,
    val submittedAt: Long? = null
)
