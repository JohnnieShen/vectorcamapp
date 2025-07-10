package com.vci.vectorcamapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VectorCamApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        val postHogConfig = PostHogAndroidConfig(
            apiKey = BuildConfig.POSTHOG_API_KEY,
            host = BuildConfig.POSTHOG_HOST
        )

        postHogConfig.captureApplicationLifecycleEvents = true

        postHogConfig.sessionReplay = true

        postHogConfig.sessionReplayConfig.maskAllImages = false
        postHogConfig.sessionReplayConfig.maskAllTextInputs = false
        postHogConfig.sessionReplayConfig.screenshot = true

        postHogConfig.optOut = BuildConfig.DEBUG

        PostHogAndroid.setup(this, postHogConfig)
    }
}
