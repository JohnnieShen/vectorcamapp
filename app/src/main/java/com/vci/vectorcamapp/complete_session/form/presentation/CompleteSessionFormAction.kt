package com.vci.vectorcamapp.complete_session.form.presentation

import java.util.UUID

sealed interface CompleteSessionFormAction {
    data class LoadSession(val sessionId: UUID) : CompleteSessionFormAction
}
