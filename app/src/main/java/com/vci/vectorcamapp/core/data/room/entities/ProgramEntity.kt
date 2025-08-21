package com.vci.vectorcamapp.core.data.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "program", indices = [Index("country")])
data class ProgramEntity(
    @PrimaryKey val id: Int = -1,
    val name: String = "",
    val country: String = "",
)
