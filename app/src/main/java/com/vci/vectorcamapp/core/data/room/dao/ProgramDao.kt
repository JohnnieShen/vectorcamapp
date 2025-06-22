package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vci.vectorcamapp.core.data.room.entities.ProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(programs: List<ProgramEntity>)

    @Query("SELECT * FROM program")
    suspend fun getAllPrograms(): List<ProgramEntity>
}
