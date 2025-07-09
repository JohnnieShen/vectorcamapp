package com.vci.vectorcamapp.incomplete_session.presentation

import java.util.UUID

sealed interface IncompleteSessionAction {
    data class ResumeSession(val sessionId: UUID) : IncompleteSessionAction
    data object ReturnToLandingScreen : IncompleteSessionAction
}