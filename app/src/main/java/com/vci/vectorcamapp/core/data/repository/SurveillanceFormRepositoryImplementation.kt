package com.vci.vectorcamapp.core.data.repository

import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.SurveillanceFormDao
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.repository.SurveillanceFormRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import java.util.UUID
import javax.inject.Inject

class SurveillanceFormRepositoryImplementation @Inject constructor(
    private val surveillanceFormDao: SurveillanceFormDao
) : SurveillanceFormRepository {
    override suspend fun upsertSurveillanceForm(
        surveillanceForm: SurveillanceForm, sessionId: UUID
    ): Result<Unit, RoomDbError> {
        return try {
            surveillanceFormDao.upsertSurveillanceForm(surveillanceForm.toEntity(sessionId))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getSurveillanceFormBySessionId(sessionId: UUID): SurveillanceForm? =
        surveillanceFormDao
            .getSurveillanceFormBySessionId(sessionId)
            ?.toDomain()
}
