package com.vci.vectorcamapp.imaging.presentation

sealed interface ImagingEvent {
    data object NavigateBackToLandingScreen : ImagingEvent
}
