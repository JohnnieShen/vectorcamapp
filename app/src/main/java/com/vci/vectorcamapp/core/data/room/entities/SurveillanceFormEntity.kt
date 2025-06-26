package com.vci.vectorcamapp.core.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "surveillance_form", foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["localId"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class SurveillanceFormEntity(
    @PrimaryKey val sessionId: UUID = UUID(0, 0),
    val numPeopleSleptInHouse: Int = 0,
    val wasIrsConducted: Boolean = false,
    val monthsSinceIrs: Int? = null,
    val numLlinsAvailable: Int = 0,
    val llinType: String? = null,
    val llinBrand: String? = null,
    val numPeopleSleptUnderLlin: Int? = null,
    val submittedAt: Long?
)
