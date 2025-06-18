package com.vci.vectorcamapp.core.data.cache

import androidx.datastore.core.DataStore
import com.vci.vectorcamapp.core.data.dto.SessionDto
import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toDto
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.model.Session
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CurrentSessionCacheImplementation @Inject constructor(
    private val dataStore: DataStore<SessionDto>
) : CurrentSessionCache {
    override suspend fun saveSession(session: Session, siteId: Int) {
        dataStore.updateData {
            session.toDto(siteId)
        }
    }

    override suspend fun getSession(): Session? {
        val sessionDto = dataStore.data.firstOrNull()
        return if (sessionDto == null || sessionDto.isEmpty()) null else sessionDto.toDomain()
    }

    override suspend fun clearSession() {
        dataStore.updateData {
            SessionDto()
        }
    }

    override suspend fun getSiteId(): Int? {
        val sessionDto = dataStore.data.firstOrNull()
        return if (sessionDto == null || sessionDto.isEmpty()) null
        else sessionDto.siteId
    }
}
