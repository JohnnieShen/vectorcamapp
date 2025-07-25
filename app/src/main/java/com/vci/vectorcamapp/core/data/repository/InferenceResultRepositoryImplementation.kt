package com.vci.vectorcamapp.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.InferenceResultDao
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.repository.InferenceResultRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import java.util.UUID
import javax.inject.Inject

class InferenceResultRepositoryImplementation @Inject constructor(
    private val inferenceResultDao: InferenceResultDao
) : InferenceResultRepository {
    override suspend fun insertInferenceResult(inferenceResult: InferenceResult, specimenImageId: UUID): Result<Unit, RoomDbError> {
        return try {
            inferenceResultDao.insertInferenceResult(inferenceResult.toEntity(specimenImageId))
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.CONSTRAINT_VIOLATION)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateInferenceResult(inferenceResult: InferenceResult, specimenImageId: UUID): Result<Unit, RoomDbError> {
        return try {
            inferenceResultDao.updateInferenceResult(inferenceResult.toEntity(specimenImageId))
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.CONSTRAINT_VIOLATION)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }
}