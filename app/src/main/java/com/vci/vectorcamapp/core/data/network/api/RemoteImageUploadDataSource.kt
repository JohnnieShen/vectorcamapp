package com.vci.vectorcamapp.core.data.network.api

import android.util.Log
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.upload.image.util.UploadError
import com.vci.vectorcamapp.core.domain.network.api.ImageUploadDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

class RemoteImageUploadDataSource @Inject constructor(
    private val httpClient: HttpClient
) : ImageUploadDataSource {
    override suspend fun imageExists(specimenId: String, md5: String): Result<URL, UploadError> = withContext(Dispatchers.IO) {
        try {
            val path = "specimens/$specimenId/images/$md5"
            val response: HttpResponse = httpClient.head(constructUrl(path))
            val finalUrl = URL(response.request.url.toString())

            if (response.status == HttpStatusCode.OK) {
                Result.Success(finalUrl)
            } else {
                Result.Error(UploadError.VERIFICATION_ERROR)
            }
        } catch (e: Exception) {
            Log.w("RemoteImageUploadDataSource", "Image existence check failed for specimen '$specimenId' and md5 '$md5'", e)
            Result.Error(UploadError.NO_INTERNET)
        }
    }
}