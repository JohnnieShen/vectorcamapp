package com.vci.vectorcamapp.core.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

@Entity(
    tableName = "specimen", foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["localId"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )], indices = [Index("sessionId")], primaryKeys = ["id", "sessionId"]
)
data class SpecimenEntity(
    val id: String = "",
    val sessionId: UUID = UUID(0, 0),
)
