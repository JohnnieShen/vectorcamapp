package com.vci.vectorcamapp.core.data.upload.image.util

import io.tus.java.client.TusClient
import java.net.HttpURLConnection

class TimeoutConfiguredTusClient : TusClient() {

    companion object {
        private const val CONNECTION_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 30000
    }

    override fun prepareConnection(connection: HttpURLConnection) {
        super.prepareConnection(connection)
        connection.connectTimeout = CONNECTION_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
    }
}
