package com.vci.vectorcamapp.core.data.dto.session

import com.vci.vectorcamapp.core.data.dto.serializers.UuidSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SessionDto(
    val sessionId: Int? = null,
    @Serializable(with = UuidSerializer::class)
    val frontendId: UUID = UUID(0, 0),
    val houseNumber: String = "",
    val collectorTitle: String = "",
    val collectorName: String = "",
    val collectionDate: Long = 0L,
    val collectionMethod: String = "",
    val specimenCondition: String = "",
    val createdAt: Long = 0L,
    val completedAt: Long? = null,
    val submittedAt: Long? = null,
    val notes: String = "",
    val latitude: Float? = null,
    val longitude: Float? = null,
    val siteId: Int = -1,
    val deviceId: Int = -1,
)
