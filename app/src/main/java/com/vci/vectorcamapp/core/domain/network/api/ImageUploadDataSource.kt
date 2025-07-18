package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.upload.image.util.UploadError
import com.vci.vectorcamapp.core.domain.util.Result
import java.net.URL

interface ImageUploadDataSource {
    suspend fun imageExists(specimenId: String, md5: String): Result<URL, UploadError>
}