package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vci.vectorcamapp.core.data.room.entities.SpecimenEntity
import com.vci.vectorcamapp.core.data.room.entities.relations.SpecimenImageAndInferenceResultRelation
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SpecimenDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpecimen(specimen: SpecimenEntity)

    @Query("SELECT * FROM specimen WHERE id = :specimenId AND sessionId = :sessionId")
    suspend fun getSpecimenByIdAndSessionId(specimenId: String, sessionId: UUID): SpecimenEntity?

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateSpecimen(specimen: SpecimenEntity)

    @Delete
    suspend fun deleteSpecimen(specimen: SpecimenEntity): Int

    @Query("SELECT * FROM specimen WHERE sessionId = :sessionId")
    suspend fun getSpecimensBySession(sessionId: UUID): List<SpecimenEntity>

    @Query("SELECT * FROM specimen WHERE sessionId = :sessionId")
    fun observeSpecimensBySession(sessionId: UUID): Flow<List<SpecimenEntity>>

    @Transaction
    @Query("SELECT * FROM specimen_image WHERE specimenId = :specimenId AND sessionId = :sessionId")
    suspend fun getSpecimenImagesAndInferenceResultsBySpecimen(
        specimenId: String,
        sessionId: UUID
    ): List<SpecimenImageAndInferenceResultRelation>

    @Transaction
    @Query("SELECT * FROM specimen_image WHERE specimenId = :specimenId AND sessionId = :sessionId")
    fun observeSpecimenImagesAndInferenceResultsBySpecimen(
        specimenId: String,
        sessionId: UUID
    ): Flow<List<SpecimenImageAndInferenceResultRelation>>
}
