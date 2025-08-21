package com.vci.vectorcamapp.core.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "site", foreignKeys = [ForeignKey(
        entity = ProgramEntity::class,
        parentColumns = ["id"],
        childColumns = ["programId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )], indices = [Index("programId")]
)
data class SiteEntity(
    @PrimaryKey val id: Int = -1,
    val programId: Int = -1,
    val district: String = "",
    val subCounty: String = "",
    val parish: String = "",
    val sentinelSite: String = "",
    val healthCenter: String = "",
)
