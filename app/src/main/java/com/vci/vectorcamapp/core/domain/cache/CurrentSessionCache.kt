package com.vci.vectorcamapp.core.domain.cache

import com.vci.vectorcamapp.core.domain.model.Session

interface CurrentSessionCache {
    suspend fun saveSession(session: Session, siteId: Int)
    suspend fun getSession() : Session?
    suspend fun clearSession()
    suspend fun getSiteId(): Int?
}
