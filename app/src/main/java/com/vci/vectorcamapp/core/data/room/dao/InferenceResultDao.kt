package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.vci.vectorcamapp.core.data.room.entities.InferenceResultEntity

@Dao
interface InferenceResultDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInferenceResult(inferenceResultEntity: InferenceResultEntity)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateInferenceResult(inferenceResultEntity: InferenceResultEntity)
}
