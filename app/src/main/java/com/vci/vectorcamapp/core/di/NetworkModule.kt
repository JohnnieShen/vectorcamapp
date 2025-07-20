package com.vci.vectorcamapp.core.di

import android.content.Context
import com.vci.vectorcamapp.core.data.upload.image.util.TimeoutConfiguredTusClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.tus.android.client.TusPreferencesURLStore
import io.tus.java.client.TusClient
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.ANDROID
            }
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    prettyPrint = false
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideTusClient(
        @ApplicationContext context: Context
    ): TusClient {
        return TimeoutConfiguredTusClient().apply {
            headers = mapOf("Content-Type" to "application/offset+octet-stream")
            enableResuming(TusPreferencesURLStore(context.getSharedPreferences("tus_worker", Context.MODE_PRIVATE)))
        }
    }
}