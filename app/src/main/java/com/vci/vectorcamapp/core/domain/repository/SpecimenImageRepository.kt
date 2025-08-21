package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import java.util.UUID

interface SpecimenImageRepository {
    suspend fun insertSpecimenImage(specimenImage: SpecimenImage, specimenId: String, sessionId: UUID) : Result<Unit, RoomDbError>
    suspend fun updateSpecimenImage(specimenImage: SpecimenImage, specimenId: String, sessionId: UUID) : Result<Unit, RoomDbError>
    suspend fun deleteSpecimenImage(specimenImage: SpecimenImage, specimenId: String, sessionId: UUID) : Boolean
}
