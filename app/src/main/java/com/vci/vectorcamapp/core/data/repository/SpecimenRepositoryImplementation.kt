package com.vci.vectorcamapp.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.SpecimenDao
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenImageAndInferenceResult
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenWithSpecimenImagesAndInferenceResults
import com.vci.vectorcamapp.core.domain.repository.SpecimenRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SpecimenRepositoryImplementation @Inject constructor(
    private val specimenDao: SpecimenDao
) : SpecimenRepository {
    override suspend fun insertSpecimen(
        specimen: Specimen, sessionId: UUID
    ): Result<Unit, RoomDbError> {
        return try {
            specimenDao.insertSpecimen(specimen.toEntity(sessionId))
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.CONSTRAINT_VIOLATION)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun updateSpecimen(
        specimen: Specimen, sessionId: UUID
    ): Result<Unit, RoomDbError> {
        return try {
            val updatedRows = specimenDao.updateSpecimen(specimen.toEntity(sessionId))
            if (updatedRows == 0) {
                Result.Error(RoomDbError.NO_ROWS_AFFECTED)
            }
            Result.Success(Unit)
        } catch (e: SQLiteConstraintException) {
            Result.Error(RoomDbError.CONSTRAINT_VIOLATION)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun getSpecimenByIdAndSessionId(
        specimenId: String, sessionId: UUID
    ): Specimen? {
        return specimenDao.getSpecimenByIdAndSessionId(specimenId, sessionId)?.toDomain()
    }

    override suspend fun deleteSpecimen(specimen: Specimen, sessionId: UUID): Boolean {
        return specimenDao.deleteSpecimen(specimen.toEntity(sessionId)) > 0
    }

    override suspend fun getSpecimenImagesAndInferenceResultsBySession(sessionId: UUID): List<SpecimenWithSpecimenImagesAndInferenceResults> {
        val specimens = specimenDao.getSpecimensBySession(sessionId)
        return specimens.map { specimenEntity ->
            val specimenImagesAndResults =
                specimenDao.getSpecimenImagesAndInferenceResultsBySpecimen(
                    specimenEntity.id, sessionId
                )

            SpecimenWithSpecimenImagesAndInferenceResults(
                specimen = specimenEntity.toDomain(),
                specimenImagesAndInferenceResults = specimenImagesAndResults.map { relation ->
                    SpecimenImageAndInferenceResult(
                        specimenImage = relation.specimenImageEntity.toDomain(),
                        inferenceResult = relation.inferenceResultEntity?.toDomain()
                    )
                })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSpecimenImagesAndInferenceResultsBySession(
        sessionId: UUID
    ): Flow<List<SpecimenWithSpecimenImagesAndInferenceResults>> {
        return specimenDao.observeSpecimensBySession(sessionId).flatMapLatest { specimenEntities ->
            if (specimenEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    specimenEntities.map { specimenEntity ->
                        specimenDao.observeSpecimenImagesAndInferenceResultsBySpecimen(
                            specimenEntity.id, sessionId
                        ).map { specimenImagesAndResults ->
                            SpecimenWithSpecimenImagesAndInferenceResults(
                                specimen = specimenEntity.toDomain(),
                                specimenImagesAndInferenceResults = specimenImagesAndResults.map { relation ->
                                    SpecimenImageAndInferenceResult(
                                        specimenImage = relation.specimenImageEntity.toDomain(),
                                        inferenceResult = relation.inferenceResultEntity?.toDomain()
                                    )
                                })
                        }
                    }) { it.toList() }
            }
        }
    }
}
