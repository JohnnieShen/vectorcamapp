package com.vci.vectorcamapp.main.logging

import com.vci.vectorcamapp.core.logging.Crashy
import com.vci.vectorcamapp.core.logging.CrashyContext
import io.sentry.SentryLevel

object MainSentryLogger {

    fun openCvInitFailure(e: Throwable) {
        Crashy.exception(
            throwable = e,
            context = CrashyContext(
                screen = "AppStart",
                feature = "OpenCV Initialization",
                action = "initLocal()"
            ),
            tags = mapOf(
                "module" to "OpenCV",
                "phase" to "startup"
            ),
            extras = mapOf(
                "deviceModel" to android.os.Build.MODEL,
                "sdkVersion" to android.os.Build.VERSION.SDK_INT,
            )
        )
    }

    fun postHogInitFailure(e: Throwable) {
        Crashy.exception(
            throwable = e,
            context = CrashyContext(
                screen = "AppStart",
                feature = "PostHog Initialization",
                action = "setup()"
            ),
            tags = mapOf(
                "module" to "PostHog",
                "phase" to "startup"
            ),
            extras = mapOf(
                "deviceModel" to android.os.Build.MODEL,
                "sdkVersion" to android.os.Build.VERSION.SDK_INT,
            )
        )
    }

    fun deviceFetchFailure(e: Throwable) {
        Crashy.exception(
            throwable = e,
            context = CrashyContext(
                screen = "Main",
                feature = "DeviceCache",
                action = "observe_program_id"
            ),
            tags = mapOf(
                "error_type" to "device_cache_failure",
                "critical" to "true",
                "phase" to "startup",
            ),
            extras = mapOf(
                "fallback_destination" to "Registration",
                "error_impact" to "User redirected to registration",
                "recovery_action" to "Defaulting to registration flow"
            )
        )
    }
}
