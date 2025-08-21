package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vci.vectorcamapp.core.data.room.entities.ProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(programs: List<ProgramEntity>)

    @Query("SELECT * FROM program")
    fun observeAllPrograms(): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM program WHERE id = :id")
    suspend fun getProgramById(id: Int): ProgramEntity?
}
