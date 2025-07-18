package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import java.net.URL

interface ImageUploadDataSource {
    suspend fun imageExists(specimenId: String, md5: String): Result<URL, NetworkError>
}