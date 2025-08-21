package com.vci.vectorcamapp.main.presentation

sealed interface MainAction {
    data object RequestPermissions : MainAction
    data object OpenAppSettings : MainAction
    data object OpenLocationSettings : MainAction
    data class UpdatePermissionStatus(val allGranted: Boolean) : MainAction
    data class UpdateGpsStatus(val isGpsEnabled: Boolean) : MainAction
}
