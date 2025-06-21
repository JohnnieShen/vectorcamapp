package com.vci.vectorcamapp.complete_session.specimens.presentation

import java.util.UUID

sealed interface CompleteSessionSpecimensAction {
    data class LoadSession(val sessionId: UUID) : CompleteSessionSpecimensAction
}
