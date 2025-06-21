package com.vci.vectorcamapp.landing.presentation

sealed interface LandingEvent {
    data object NavigateToNewSurveillanceSessionScreen: LandingEvent
    data object NavigateToIncompleteSessionsScreen: LandingEvent
    data object NavigateToCompleteSessionsScreen: LandingEvent
}
