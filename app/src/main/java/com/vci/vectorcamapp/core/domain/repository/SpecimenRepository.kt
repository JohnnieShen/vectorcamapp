package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenWithSpecimenImagesAndInferenceResults
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface SpecimenRepository {
    suspend fun insertSpecimen(specimen: Specimen, sessionId: UUID): Result<Unit, RoomDbError>
    suspend fun getSpecimenByIdAndSessionId(specimenId: String, sessionId: UUID): Specimen?
    suspend fun updateSpecimen(specimen: Specimen, sessionId: UUID): Result<Unit, RoomDbError>
    suspend fun deleteSpecimen(specimen: Specimen, sessionId: UUID): Boolean
    suspend fun getSpecimenImagesAndInferenceResultsBySession(sessionId: UUID): List<SpecimenWithSpecimenImagesAndInferenceResults>
    fun observeSpecimenImagesAndInferenceResultsBySession(sessionId: UUID): Flow<List<SpecimenWithSpecimenImagesAndInferenceResults>>
}
