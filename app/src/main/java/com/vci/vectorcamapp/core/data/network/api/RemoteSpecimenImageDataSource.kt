package com.vci.vectorcamapp.core.data.network.api

import com.vci.vectorcamapp.core.data.dto.specimen_image.SpecimenImageDto
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.network.safeCall
import com.vci.vectorcamapp.core.domain.network.api.SpecimenImageDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class RemoteSpecimenImageDataSource @Inject constructor(
    private val httpClient: HttpClient
): SpecimenImageDataSource {

    override suspend fun checkImageExists(
        specimenId: String,
        imageId: Int
    ): Result<SpecimenImageDto, NetworkError> {
        return safeCall<SpecimenImageDto> {
            httpClient.get(constructUrl("/specimens/$specimenId/images/data/$imageId")) {
                contentType(ContentType.Application.Json)
            }
        }
    }

}