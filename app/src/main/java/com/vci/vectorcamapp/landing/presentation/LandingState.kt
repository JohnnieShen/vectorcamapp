package com.vci.vectorcamapp.landing.presentation

import com.vci.vectorcamapp.BuildConfig
import com.vci.vectorcamapp.core.domain.model.Session

data class LandingState(
    val versionName: String = BuildConfig.VERSION_NAME,
    val isLoading: Boolean = false,
    val incompleteSessions: List<Session> = emptyList(),
    val completeSessions: List<Session> = emptyList(),
    val showResumeDialog: Boolean = false
)
