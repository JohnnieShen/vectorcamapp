package com.vci.vectorcamapp.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.SpecimenDao
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenAndBoundingBox
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SpecimenRepositoryImplementation @Inject constructor(
    private val specimenDao: SpecimenDao
) : SpecimenRepository {
    override suspend fun insertSpecimen(specimen: Specimen, sessionId: UUID): Result<Unit, RoomDbError> {
        return try {
            specimenDao.insertSpecimen(specimen.toEntity(sessionId))
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.DUPLICATE_SPECIMEN_ID)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteSpecimen(specimen: Specimen, sessionId: UUID): Boolean {
        return specimenDao.deleteSpecimen(specimen.toEntity(sessionId)) > 0
    }

    override fun observeSpecimenAndBoundingBox(specimenId: String): Flow<SpecimenAndBoundingBox?> {
        return specimenDao.observeSpecimenAndBoundingBox(specimenId).map { specimenAndBoundingBoxRelation ->
            specimenAndBoundingBoxRelation?.let {
                SpecimenAndBoundingBox(
                    specimen = it.specimenEntity.toDomain(),
                    boundingBox = it.boundingBoxEntity.toDomain()
                )
            }
        }
    }

    override fun observeSpecimensAndBoundingBoxesBySession(sessionId: UUID): Flow<List<SpecimenAndBoundingBox>> {
        return specimenDao.observeSpecimensAndBoundingBoxesBySession(sessionId)
            .map { specimenAndBoundingBoxRelations ->
                specimenAndBoundingBoxRelations.map {
                    SpecimenAndBoundingBox(
                        specimen = it.specimenEntity.toDomain(),
                        boundingBox = it.boundingBoxEntity.toDomain()
                    )
                }
            }
    }
}
