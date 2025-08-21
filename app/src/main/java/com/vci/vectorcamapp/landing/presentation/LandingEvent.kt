package com.vci.vectorcamapp.landing.presentation

import com.vci.vectorcamapp.core.domain.model.enums.SessionType

sealed interface LandingEvent {
    data class NavigateToIntakeScreen(val sessionType: SessionType): LandingEvent
    data object NavigateToIncompleteSessionsScreen: LandingEvent
    data object NavigateToCompleteSessionsScreen: LandingEvent
    data object NavigateBackToRegistrationScreen: LandingEvent
}
