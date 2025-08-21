package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import java.util.UUID

interface SurveillanceFormRepository {
    suspend fun upsertSurveillanceForm(surveillanceForm: SurveillanceForm, sessionId: UUID): Result<Unit, RoomDbError>
    suspend fun getSurveillanceFormBySessionId(sessionId: UUID): SurveillanceForm?
}
