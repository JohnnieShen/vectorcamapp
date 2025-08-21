package com.vci.vectorcamapp.core.data.repository

import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.room.dao.ProgramDao
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgramRepositoryImplementation @Inject constructor(
    private val programDao: ProgramDao
) : ProgramRepository {

    override fun observeAllPrograms(): Flow<List<Program>> {
        return programDao.observeAllPrograms().map { programEntities ->
            programEntities.map { it.toDomain() }
        }
    }

    override suspend fun getProgramById(programId: Int): Program? {
        return programDao.getProgramById(programId)?.toDomain()
    }
}
