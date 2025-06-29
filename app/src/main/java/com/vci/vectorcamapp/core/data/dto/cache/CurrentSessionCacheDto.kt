package com.vci.vectorcamapp.core.data.dto.cache

import com.vci.vectorcamapp.core.data.dto.serializers.UuidSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CurrentSessionCacheDto(
    @Serializable(with = UuidSerializer::class)
    val localId: UUID = UUID(0, 0),
    val siteId: Int = -1,
    val remoteId: Int? = null,
    val houseNumber: String = "",
    val collectorTitle: String = "",
    val collectorName: String = "",
    val collectionDate: Long = 0L,
    val collectionMethod: String = "",
    val specimenCondition: String = "",
    val createdAt: Long = 0L,
    val completedAt: Long? = null,
    val submittedAt: Long? = null,
    val notes: String = ""
) {
    fun isEmpty() = localId == UUID(0, 0) && createdAt == 0L
}
