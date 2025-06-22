package com.vci.vectorcamapp.core.data.repository

import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.room.dao.ProgramDao
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.core.domain.repository.ProgramRepository
import javax.inject.Inject

class ProgramRepositoryImplementation @Inject constructor(
    private val programDao: ProgramDao
) : ProgramRepository {

    override suspend fun getAllPrograms(): List<Program> {
        return programDao.getAllPrograms().map { it.toDomain() }
    }
}