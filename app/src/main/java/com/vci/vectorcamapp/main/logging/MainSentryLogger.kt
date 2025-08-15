package com.vci.vectorcamapp.main.logging

import com.vci.vectorcamapp.core.logging.Crashy
import com.vci.vectorcamapp.core.logging.CrashyContext

object MainSentryLogger {

    fun logOpenCvInitFailure(e: Throwable) {
        Crashy.exception(
            throwable = e, context = CrashyContext(
                screen = "AppStart", feature = "OpenCV Initialization", action = "initLocal()"
            ), tags = mapOf(
                "module" to "OpenCV", "phase" to "startup"
            ), extras = mapOf(
                "deviceModel" to android.os.Build.MODEL,
                "sdkVersion" to android.os.Build.VERSION.SDK_INT,
                "possible_causes" to "OpenCV not bundled properly in APK, initLocal() called too early, ABI mismatch, missing native libs"
            )
        )
    }

    fun logPostHogInitFailure(e: Throwable) {
        Crashy.exception(
            throwable = e, context = CrashyContext(
                screen = "AppStart", feature = "PostHog Initialization", action = "setup()"
            ), tags = mapOf(
                "module" to "PostHog", "phase" to "startup"
            ), extras = mapOf(
                "deviceModel" to android.os.Build.MODEL,
                "sdkVersion" to android.os.Build.VERSION.SDK_INT,
                "possible_causes" to "Incorrect API key, network error during setup, SDK not initialized in Application, app context issues"
            )
        )
    }

    fun logDeviceFetchFailure(e: Throwable) {
        Crashy.exception(
            throwable = e, context = CrashyContext(
                screen = "Main", feature = "DeviceCache", action = "observe_program_id"
            ), tags = mapOf(
                "error_type" to "device_fetch_failure",
                "critical" to "true",
                "phase" to "startup",
            ), extras = mapOf(
                "fallback_destination" to "Registration",
                "error_context" to "User redirected to registration",
                "recovery_action" to "Defaulting to registration flow",
                "possible_causes" to "Program ID not cached, database not initialized, device configuration incomplete, corruption in device cache"
            )
        )
    }
}
