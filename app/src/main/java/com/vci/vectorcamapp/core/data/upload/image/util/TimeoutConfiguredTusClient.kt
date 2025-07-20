package com.vci.vectorcamapp.core.data.upload.image.util

import io.tus.java.client.TusClient
import java.net.HttpURLConnection

class TimeoutConfiguredTusClient : TusClient() {
    companion object {
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 30_000
    }

    override fun prepareConnection(connection: HttpURLConnection) {
        super.prepareConnection(connection)

        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
    }
}