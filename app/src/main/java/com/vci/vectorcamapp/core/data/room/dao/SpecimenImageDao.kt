package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.vci.vectorcamapp.core.data.room.entities.SpecimenImageEntity

@Dao
interface SpecimenImageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpecimenImage(specimenImage: SpecimenImageEntity)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateSpecimenImage(specimenImage: SpecimenImageEntity): Int

    @Delete
    suspend fun deleteSpecimenImage(specimenImage: SpecimenImageEntity): Int
}