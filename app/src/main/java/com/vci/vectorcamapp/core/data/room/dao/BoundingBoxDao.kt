package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.vci.vectorcamapp.core.data.room.entities.BoundingBoxEntity

@Dao
interface BoundingBoxDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBoundingBox(boundingBoxEntity: BoundingBoxEntity)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateBoundingBox(boundingBoxEntity: BoundingBoxEntity)
}
