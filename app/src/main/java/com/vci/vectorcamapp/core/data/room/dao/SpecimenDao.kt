package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vci.vectorcamapp.core.data.room.entities.SpecimenEntity
import com.vci.vectorcamapp.core.data.room.entities.relations.SpecimenAndBoundingBoxRelation
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SpecimenDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpecimen(specimen: SpecimenEntity)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateSpecimen(specimen: SpecimenEntity)

    @Delete
    suspend fun deleteSpecimen(specimen: SpecimenEntity): Int

    @Query("SELECT * FROM specimen WHERE id = :id")
    suspend fun getSpecimenById(id: String): SpecimenEntity?

    @Transaction
    @Query("SELECT * FROM specimen WHERE sessionId = :sessionId")
    fun getSpecimensAndBoundingBoxesBySession(sessionId: UUID): List<SpecimenAndBoundingBoxRelation>

    @Transaction
    @Query("SELECT * FROM specimen WHERE sessionId = :sessionId")
    fun observeSpecimensAndBoundingBoxesBySession(sessionId: UUID): Flow<List<SpecimenAndBoundingBoxRelation>>
}
