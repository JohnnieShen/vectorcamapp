package com.vci.vectorcamapp.core.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import java.util.UUID

@Entity(
    tableName = "session", foreignKeys = [ForeignKey(
        entity = SiteEntity::class,
        parentColumns = ["id"],
        childColumns = ["siteId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )], indices = [Index("completedAt"), Index("siteId"), Index("type")]
)
data class SessionEntity(
    @PrimaryKey val localId: UUID = UUID(0, 0),
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
    val notes: String = "",
    val latitude: Float? = null,
    val longitude: Float? = null,
    val type: SessionType,
)
