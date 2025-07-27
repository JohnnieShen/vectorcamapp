package com.vci.vectorcamapp.incomplete_session.presentation

import com.vci.vectorcamapp.core.domain.model.enums.SessionType

sealed interface IncompleteSessionEvent {
    data class NavigateToIntakeScreen(val sessionType: SessionType) : IncompleteSessionEvent
    data object NavigateToLandingScreen : IncompleteSessionEvent
}