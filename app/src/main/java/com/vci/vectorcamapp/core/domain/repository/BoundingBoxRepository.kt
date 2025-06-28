package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError

interface BoundingBoxRepository {
    suspend fun insertBoundingBox(boundingBox: BoundingBox, specimenId: String) : Result<Unit, RoomDbError>
    suspend fun updateBoundingBox(boundingBox: BoundingBox, specimenId: String) : Result<Unit, RoomDbError>
}