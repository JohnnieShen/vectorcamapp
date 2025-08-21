package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.vci.vectorcamapp.core.data.room.entities.SessionEntity
import com.vci.vectorcamapp.core.data.room.entities.relations.SessionAndSurveillanceFormRelation
import com.vci.vectorcamapp.core.data.room.entities.relations.SessionAndSiteRelation
import com.vci.vectorcamapp.core.data.room.entities.relations.SessionWithSpecimensRelation
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SessionDao {

    @Upsert
    suspend fun upsertSession(session: SessionEntity): Long

    @Query("SELECT * FROM session WHERE localId = :sessionId")
    suspend fun getSessionById(sessionId: UUID): SessionEntity?

    @Delete
    suspend fun deleteSession(session: SessionEntity): Int

    @Query("UPDATE session SET completedAt = :timestamp WHERE localId = :sessionId")
    suspend fun markSessionAsComplete(sessionId: UUID, timestamp: Long): Int

    @Transaction
    @Query("SELECT * FROM session WHERE localId = :sessionId")
    suspend fun getSessionWithSpecimens(sessionId: UUID): SessionWithSpecimensRelation?

    @Transaction
    @Query("SELECT * FROM session WHERE localId = :sessionId")
    suspend fun getSessionAndSurveillanceForm(sessionId: UUID): SessionAndSurveillanceFormRelation?

    @Transaction
    @Query("SELECT * FROM session WHERE localId = :sessionId")
    suspend fun getSessionAndSiteById(sessionId: UUID): SessionAndSiteRelation?

    @Query("SELECT * FROM session WHERE completedAt IS NULL")
    fun observeIncompleteSessions(): Flow<List<SessionEntity>>

    @Transaction
    @Query("SELECT * FROM session WHERE completedAt IS NOT NULL")
    fun observeCompleteSessionsAndSites(): Flow<List<SessionAndSiteRelation>>

    @Transaction
    @Query("SELECT * FROM session WHERE localId = :sessionId")
    fun observeSessionWithSpecimens(sessionId: UUID): Flow<SessionWithSpecimensRelation?>
}
