package com.vci.vectorcamapp.core.data.cache

import androidx.datastore.core.DataStore
import com.vci.vectorcamapp.core.data.dto.cache.CurrentSessionCacheDto
import com.vci.vectorcamapp.core.domain.cache.CurrentSessionCache
import com.vci.vectorcamapp.core.domain.model.Session
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CurrentSessionCacheImplementation @Inject constructor(
    private val dataStore: DataStore<CurrentSessionCacheDto>
) : CurrentSessionCache {
    override suspend fun saveSession(session: Session, siteId: Int) {
        dataStore.updateData {
            CurrentSessionCacheDto(
                localId = session.localId,
                siteId = siteId,
                remoteId = session.remoteId,
                houseNumber = session.houseNumber,
                collectorTitle = session.collectorTitle,
                collectorName = session.collectorName,
                collectionDate = session.collectionDate,
                collectionMethod = session.collectionMethod,
                specimenCondition = session.specimenCondition,
                createdAt = session.createdAt,
                completedAt = session.completedAt,
                submittedAt = session.submittedAt,
                notes = session.notes,
                latitude = session.latitude,
                longitude = session.longitude,
                type = session.type
            )
        }
    }

    override suspend fun getSession(): Session? {
        val currentSessionCacheDto = dataStore.data.firstOrNull()
        return if (currentSessionCacheDto == null || currentSessionCacheDto.isEmpty()) {
            null
        } else {
            Session(
                localId = currentSessionCacheDto.localId,
                remoteId = currentSessionCacheDto.remoteId,
                houseNumber = currentSessionCacheDto.houseNumber,
                collectorTitle = currentSessionCacheDto.collectorTitle,
                collectorName = currentSessionCacheDto.collectorName,
                collectionDate = currentSessionCacheDto.collectionDate,
                collectionMethod = currentSessionCacheDto.collectionMethod,
                specimenCondition = currentSessionCacheDto.specimenCondition,
                createdAt = currentSessionCacheDto.createdAt,
                completedAt = currentSessionCacheDto.completedAt,
                submittedAt = currentSessionCacheDto.submittedAt,
                notes = currentSessionCacheDto.notes,
                latitude = currentSessionCacheDto.latitude,
                longitude = currentSessionCacheDto.longitude,
                type = currentSessionCacheDto.type
            )
        }
    }

    override suspend fun clearSession() {
        dataStore.updateData {
            CurrentSessionCacheDto()
        }
    }

    override suspend fun getSiteId(): Int? {
        val currentSessionCacheDto = dataStore.data.firstOrNull()
        return if (currentSessionCacheDto == null || currentSessionCacheDto.isEmpty()) {
            null
        } else {
            currentSessionCacheDto.siteId
        }
    }
}
