package com.vci.vectorcamapp.navigation

import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Registration : Destination

    @Serializable
    data object Landing : Destination

    @Serializable
    data class Intake(val sessionType: SessionType) : Destination

    @Serializable
    data object Imaging : Destination

    @Serializable
    data object IncompleteSession : Destination

    @Serializable
    data object CompleteSessionList : Destination

    @Serializable
    data class CompleteSessionDetails(val sessionId: String) : Destination
}
