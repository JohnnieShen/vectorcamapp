package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.composites.SessionAndSurveillanceForm
import com.vci.vectorcamapp.core.domain.model.composites.SessionAndSite
import com.vci.vectorcamapp.core.domain.model.composites.SessionWithSpecimens
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.room.RoomDbError
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface SessionRepository {
    suspend fun upsertSession(session: Session, siteId: Int): Result<Unit, RoomDbError>
    suspend fun deleteSession(session: Session, siteId: Int): Boolean
    suspend fun markSessionAsComplete(sessionId: UUID): Boolean
    suspend fun getSessionWithSpecimens(sessionId: UUID): SessionWithSpecimens?
    suspend fun getSessionAndSurveillanceForm(sessionId: UUID): SessionAndSurveillanceForm?
    suspend fun getSessionAndSiteById(sessionId: UUID): SessionAndSite?
    fun observeIncompleteSessions(): Flow<List<Session>>
    fun observeCompleteSessionsAndSites(): Flow<List<SessionAndSite>>
    fun observeSessionWithSpecimens(sessionId: UUID): Flow<SessionWithSpecimens?>
}
