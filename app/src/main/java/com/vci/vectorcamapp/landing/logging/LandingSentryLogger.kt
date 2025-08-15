package com.vci.vectorcamapp.landing.logging

import android.os.Build
import com.vci.vectorcamapp.core.logging.Crashy
import com.vci.vectorcamapp.core.logging.CrashyContext
import io.sentry.SentryLevel

object LandingSentryLogger {

    fun logSessionNotFound(e: Throwable) {
        Crashy.exception(
            throwable = e, level = SentryLevel.WARNING, context = CrashyContext(
                screen = "LandingScreen", feature = "Session Management", action = "resume_session"
            ), tags = mapOf(
                "error_type" to "session_not_found",
                "component" to "landing",
                "impact" to "medium",
                "user_action" to "resume_session_failed"
            ), extras = mapOf(
                "error_context" to "User attempted to resume session but no active session found in cache",
                "recovery_action" to "User will need to start a new session",
                "business_impact" to "User workflow interrupted, potential data loss",
                "device_model" to Build.MODEL,
                "android_version" to Build.VERSION.RELEASE,
                "timestamp" to System.currentTimeMillis(),
                "cache_state" to "empty_or_corrupted"
            )
        )
    }

    fun logProgramIdNotFound(e: Throwable) {
        Crashy.exception(
            throwable = e, context = CrashyContext(
                screen = "LandingScreen",
                feature = "Device Configuration",
                action = "get_program_id"
            ), tags = mapOf(
                "error_type" to "program_id_missing",
                "component" to "device_cache",
                "impact" to "critical",
                "data_integrity" to "compromised"
            ), extras = mapOf(
                "error_context" to "Device program ID not found in cache during landing screen load",
                "recovery_action" to "Redirect user back to registration to reconfigure device",
                "business_impact" to "User cannot access main app functionality",
                "cache_state" to "program_id_missing",
            )
        )
    }

    fun logProgramNotFound(e: Throwable, failedProgramId: Int) {
        Crashy.exception(
            throwable = e, context = CrashyContext(
                screen = "LandingScreen",
                feature = "Program Management",
                action = "get_program_by_id",
                programId = failedProgramId.toString(),
            ), tags = mapOf(
                "error_type" to "program_not_found",
                "component" to "program_repository",
                "impact" to "critical",
                "data_integrity" to "corrupted"
            ), extras = mapOf(
                "error_context" to "Program not found in database for cached program ID",
                "program_id" to failedProgramId,
                "recovery_action" to "Redirect user back to registration to select valid program",
                "business_impact" to "User cannot access program-specific functionality",
                "possible_causes" to "Database corruption, program deletion, cache-database mismatch",
            )
        )
    }
}
