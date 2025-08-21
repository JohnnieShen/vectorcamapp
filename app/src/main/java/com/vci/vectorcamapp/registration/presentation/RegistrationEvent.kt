package com.vci.vectorcamapp.registration.presentation

sealed interface RegistrationEvent {
    data object NavigateToLandingScreen : RegistrationEvent
}
