package com.vci.vectorcamapp.landing.presentation

sealed interface LandingEvent {
    data object NavigateToNewSessionScreen: LandingEvent
    data object NavigateToIncompleteSessionsScreen: LandingEvent
    data object NavigateToCompleteSessionsScreen: LandingEvent
    data object NavigateBackToRegistrationScreen: LandingEvent
}
