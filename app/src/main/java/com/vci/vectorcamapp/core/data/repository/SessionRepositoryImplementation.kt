package com.vci.vectorcamapp.core.data.repository

import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.SessionDao
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.composites.SessionWithSpecimens
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SessionRepositoryImplementation @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {
    override suspend fun upsertSession(session: Session): Result<Unit, RoomDbError> {
        return try {
            sessionDao.upsertSession(session.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override fun getSessionById(sessionId: UUID): Session? {
        return sessionDao.getSessionById(sessionId)?.toDomain()
    }

    override suspend fun deleteSession(session: Session): Boolean {
        return sessionDao.deleteSession(session.toEntity()) > 0
    }

    override suspend fun markSessionAsComplete(sessionId: UUID): Boolean {
        return sessionDao.markSessionAsComplete(sessionId, System.currentTimeMillis()) > 0
    }

    override fun observeCompleteSessions(): Flow<List<Session>> {
        return sessionDao.observeCompleteSessions().map {
            it.map { sessionEntity -> sessionEntity.toDomain() }
        }
    }

    override fun observeIncompleteSessions(): Flow<List<Session>> {
        return sessionDao.observeIncompleteSessions().map {
            it.map { sessionEntity -> sessionEntity.toDomain() }
        }
    }

    override fun observeSessionWithSpecimens(sessionId: UUID): Flow<SessionWithSpecimens?> {
        return sessionDao.observeSessionWithSpecimens(sessionId).map { sessionWithSpecimensRelation ->
            sessionWithSpecimensRelation?.let {
                SessionWithSpecimens(
                    session = it.sessionEntity.toDomain(),
                    specimens = it.specimenEntities.map { specimenEntity -> specimenEntity.toDomain() }
                )
            }
        }
    }
}
