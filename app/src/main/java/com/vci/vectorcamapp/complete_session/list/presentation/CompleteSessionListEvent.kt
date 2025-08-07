package com.vci.vectorcamapp.complete_session.list.presentation

import java.util.UUID

sealed interface CompleteSessionListEvent {
    data object NavigateBackToLandingScreen : CompleteSessionListEvent
    data class NavigateToCompleteSessionDetails(val sessionId: UUID) : CompleteSessionListEvent
}
