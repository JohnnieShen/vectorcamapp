package com.vci.vectorcamapp.core.data.upload.image.util

import io.tus.java.client.TusClient
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named

class TimeoutConfiguredTusClient @Inject constructor(
    @Named("ConnectTimeout") private val connectTimeoutMs: Int,
    @Named("ReadTimeout") private val readTimeoutMs: Int
) : TusClient() {

    override fun prepareConnection(connection: HttpURLConnection) {
        super.prepareConnection(connection)
        connection.connectTimeout = connectTimeoutMs
        connection.readTimeout = readTimeoutMs
    }
}