package com.vci.vectorcamapp.core.domain.repository

import com.vci.vectorcamapp.core.domain.model.Site

interface SiteRepository {
    suspend fun getAllSitesByProgramId(programId: Int): List<Site>
}
