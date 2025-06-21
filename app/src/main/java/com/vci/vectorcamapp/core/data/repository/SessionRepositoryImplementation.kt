package com.vci.vectorcamapp.core.data.repository

import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toEntity
import com.vci.vectorcamapp.core.data.room.dao.SessionDao
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.composites.SessionAndSurveillanceForm
import com.vci.vectorcamapp.core.domain.model.composites.SessionAndSite
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
    override suspend fun upsertSession(session: Session, siteId: Int): Result<Unit, RoomDbError> {
        return try {
            sessionDao.upsertSession(session.toEntity(siteId))
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(RoomDbError.UNKNOWN_ERROR)
        }
    }

    override suspend fun deleteSession(session: Session, siteId: Int): Boolean {
        return sessionDao.deleteSession(session.toEntity(siteId)) > 0
    }

    override suspend fun markSessionAsComplete(sessionId: UUID): Boolean {
        return sessionDao.markSessionAsComplete(sessionId, System.currentTimeMillis()) > 0
    }

    override suspend fun getSessionWithSpecimens(sessionId: UUID): SessionWithSpecimens? {
        val relation = sessionDao.getSessionWithSpecimens(sessionId)
        return relation?.let {
            SessionWithSpecimens(
                session = it.sessionEntity.toDomain(),
                specimens = it.specimenEntities.map { specimenEntity -> specimenEntity.toDomain() }
            )
        }
    }

    override suspend fun getSessionAndSurveillanceForm(sessionId: UUID): SessionAndSurveillanceForm? {
        val relation = sessionDao.getSessionAndSurveillanceForm(sessionId)
        return relation?.let {
            SessionAndSurveillanceForm(
                session = it.sessionEntity.toDomain(),
                surveillanceForm = it.surveillanceFormEntity.toDomain()
            )
        }
    }

    override suspend fun getSessionAndSiteById(sessionId: UUID): SessionAndSite? {
        val relation = sessionDao.getSessionAndSiteById(sessionId)
        return relation?.let {
            SessionAndSite(
                session = it.session.toDomain(),
                site = it.site.toDomain()
            )
        }
    }

    override fun observeCompleteSessionsAndSites(): Flow<List<SessionAndSite>> {
        return sessionDao.observeCompleteSessionsAndSites().map { sessionAndSiteRelations ->
            sessionAndSiteRelations.map { sessionAndSiteRelation ->
                SessionAndSite(
                    session = sessionAndSiteRelation.session.toDomain(),
                    site = sessionAndSiteRelation.site.toDomain()
                )
            }
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
