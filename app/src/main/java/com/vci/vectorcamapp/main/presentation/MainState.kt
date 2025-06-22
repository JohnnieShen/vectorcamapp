package com.vci.vectorcamapp.main.presentation

import com.vci.vectorcamapp.navigation.Destination

data class MainState(
    val startDestination: Destination? = null,
    val allGranted: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val isLoading: Boolean = false
)
