package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.Program
import kotlinx.coroutines.flow.Flow

interface ProgramRepository {
    fun observeAllPrograms(): Flow<List<Program>>
    suspend fun getProgramById(programId: Int): Program?
}