package com.vci.vectorcamapp

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
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

        try {
            OpenCVLoader.initLocal()
        } catch (e: Exception) {
            Log.e("OpenCVLoader", "OpenCVLoader.initLocal() failed", e)
        }

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
