package com.vci.vectorcamapp.core.data.network.api

import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.network.safeCall
import com.vci.vectorcamapp.core.domain.network.api.ImageUploadDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
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

    override suspend fun imageExists(
        specimenId: String,
        md5: String
    ): Result<Unit, NetworkError> {
        val path = "specimens/$specimenId/images/$md5"

        val callResult = safeCall<HttpResponse> { httpClient.head(constructUrl(path)) }

            return when (callResult) {
            is Result.Success -> {
                val response = callResult.data
                if (response.status == HttpStatusCode.OK) {
                    Result.Success(Unit)
                } else {
                    Result.Error(NetworkError.CLIENT_ERROR)
                }
            }
            is Result.Error -> {
                Result.Error(callResult.error)
            }
        }
    }
}
