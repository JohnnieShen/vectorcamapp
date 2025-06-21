package com.vci.vectorcamapp.complete_session.list.presentation

import com.vci.vectorcamapp.core.domain.model.composites.SessionAndSite

data class CompleteSessionListState (
    val sessionsAndSites: List<SessionAndSite> = emptyList()
)
