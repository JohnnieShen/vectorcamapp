package com.vci.vectorcamapp.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.BoundingBoxDao
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.core.domain.repository.BoundingBoxRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import javax.inject.Inject

class BoundingBoxRepositoryImplementation @Inject constructor(
    private val boundingBoxDao: BoundingBoxDao
) : BoundingBoxRepository {
    override suspend fun insertBoundingBox(boundingBox: BoundingBox, specimenId: String): Result<Unit, RoomDbError> {
        return try {
            boundingBoxDao.insertBoundingBox(boundingBox.toEntity(specimenId))
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.CONSTRAINT_VIOLATION)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateBoundingBox(boundingBox: BoundingBox, specimenId: String): Result<Unit, RoomDbError> {
        return try {
            boundingBoxDao.updateBoundingBox(boundingBox.toEntity(specimenId))
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.CONSTRAINT_VIOLATION)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }
}