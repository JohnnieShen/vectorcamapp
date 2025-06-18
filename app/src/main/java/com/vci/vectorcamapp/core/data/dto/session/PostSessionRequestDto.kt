package com.vci.vectorcamapp.core.data.dto.session

import kotlinx.serialization.Serializable

@Serializable
data class PostSessionRequestDto (
    val deviceId: Int = 2,
    val siteId: Int = 2,
    val createdAt: Long = 0L
)
