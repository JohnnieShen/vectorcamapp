package com.vci.vectorcamapp.intake.presentation

sealed interface IntakeEvent {
    data object NavigateToImagingScreen: IntakeEvent
    data object NavigateBackToRegistrationScreen : IntakeEvent
    data object NavigateBackToLandingScreen : IntakeEvent
}
