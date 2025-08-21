package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError

interface InferenceResultRepository {
    suspend fun insertInferenceResult(inferenceResult: InferenceResult, specimenImageId: String) : Result<Unit, RoomDbError>
    suspend fun updateInferenceResult(inferenceResult: InferenceResult, specimenImageId: String) : Result<Unit, RoomDbError>
}