package com.vci.vectorcamapp.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Registration : Destination

    @Serializable
    data object Landing : Destination

    @Serializable
    data object SurveillanceForm : Destination

    @Serializable
    data object Imaging : Destination

    @Serializable
    data object IncompleteSession : Destination

    @Serializable
    data object CompleteSessionList : Destination

    @Serializable
    data class CompleteSessionDetails(val sessionId: String) : Destination
}
