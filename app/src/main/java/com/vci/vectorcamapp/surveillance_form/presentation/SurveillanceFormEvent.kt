package com.vci.vectorcamapp.surveillance_form.presentation

sealed interface SurveillanceFormEvent {
    data object NavigateToImagingScreen: SurveillanceFormEvent
    data object NavigateBackToRegistrationScreen : SurveillanceFormEvent
    data object NavigateBackToLandingScreen : SurveillanceFormEvent
}
