package com.vci.vectorcamapp.main.presentation

sealed interface MainEvent {
    data object LaunchPermissionRequest : MainEvent
    data object NavigateToAppSettings : MainEvent
    data object NavigateToLocationSettings: MainEvent
}
