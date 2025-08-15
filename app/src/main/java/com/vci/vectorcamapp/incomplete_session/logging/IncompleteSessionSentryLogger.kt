package com.vci.vectorcamapp.incomplete_session.logging

import com.vci.vectorcamapp.core.logging.Crashy
import com.vci.vectorcamapp.core.logging.CrashyContext
import java.util.UUID

object IncompleteSessionSentryLogger {

    fun logSessionNotFound(e: Throwable, failedSessionId: UUID) {
        Crashy.exception(
            throwable = e,
            context = CrashyContext(
                screen = "IncompleteSession",
                feature = "SessionResume",
                action = "session_not_found",
                sessionId = failedSessionId.toString()
            ),
            tags = mapOf(
                "error_type" to "session_not_found",
                "user_impact" to "cannot_resume_session"
            ),
            extras = mapOf(
                "requested_session_id" to failedSessionId,
                "error_impact" to "User cannot continue their work session",
                "recovery_action" to "User may need to start new session"
            )
        )
    }

    fun logSessionRetrievalFailure(e: Throwable, failedSessionId: UUID) {
        Crashy.exception(
            throwable = e,
            context = CrashyContext(
                screen = "IncompleteSession",
                feature = "SessionResume",
                action = "session_retrieval",
                sessionId = failedSessionId.toString()
            ),
            tags = mapOf(
                "error_type" to "session_retrieval_failed",
                "critical" to "true",
                "user_blocking" to "true"
            ),
            extras = mapOf(
                "requested_session_id" to failedSessionId,
                "error_impact" to "User cannot resume their incomplete session",
                "recovery_action" to "User may need to start a new session",
                "data_loss_risk" to "Previous session work may be lost"
            )
        )
    }
}
