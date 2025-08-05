package com.vci.vectorcamapp.complete_session.list.presentation

import java.util.UUID

sealed interface CompleteSessionListAction {
    data object ReturnToLandingScreen : CompleteSessionListAction
    data class ViewCompleteSessionDetails(val sessionId: UUID) : CompleteSessionListAction
    data object UploadAllPendingSessions : CompleteSessionListAction
}
