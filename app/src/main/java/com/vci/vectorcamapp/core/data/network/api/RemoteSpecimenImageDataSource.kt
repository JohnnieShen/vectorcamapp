package com.vci.vectorcamapp.core.data.network.api

import android.util.Log
import com.vci.vectorcamapp.core.data.dto.inference_result.InferenceResultDto
import com.vci.vectorcamapp.core.data.dto.specimen_image.PostSpecimenImageRequestDto
import com.vci.vectorcamapp.core.data.dto.specimen_image.PostSpecimenImageResponseDto
import com.vci.vectorcamapp.core.data.dto.specimen_image.SpecimenImageDto
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.network.safeCall
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.domain.network.api.SpecimenImageDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class RemoteSpecimenImageDataSource @Inject constructor(
    private val httpClient: HttpClient
) : SpecimenImageDataSource {
    override suspend fun postSpecimenImageMetadata(
        specimenImage: SpecimenImage, inferenceResult: InferenceResult?, specimenId: Int
    ): Result<PostSpecimenImageResponseDto, NetworkError> {
        return safeCall<PostSpecimenImageResponseDto> {
            httpClient.post(constructUrl("specimens/${specimenId}/images/data")) {
                contentType(ContentType.Application.Json)
                setBody(
                    PostSpecimenImageRequestDto(
                        filemd5 = specimenImage.localId,
                        species = specimenImage.species,
                        sex = specimenImage.sex,
                        abdomenStatus = specimenImage.abdomenStatus,
                        capturedAt = specimenImage.capturedAt,
                        inferenceResult = inferenceResult?.let {
                            InferenceResultDto(
                                bboxTopLeftX = it.bboxTopLeftX,
                                bboxTopLeftY = it.bboxTopLeftY,
                                bboxWidth = it.bboxWidth,
                                bboxHeight = it.bboxHeight,
                                bboxConfidence = it.bboxConfidence,
                                bboxClassId = it.bboxClassId,
                                speciesLogits = it.speciesLogits,
                                sexLogits = it.sexLogits,
                                abdomenStatusLogits = it.abdomenStatusLogits,
                                speciesInferenceDuration = it.speciesInferenceDuration,
                                sexInferenceDuration = it.sexInferenceDuration,
                                abdomenStatusInferenceDuration = it.abdomenStatusInferenceDuration
                            )
                        }
                    )
                )
            }
        }
    }

    override suspend fun getSpecimenImageMetadata(
        specimenImageId: String, specimenId: Int
    ): Result<SpecimenImageDto, NetworkError> {
        return safeCall<SpecimenImageDto> {
            httpClient.get(constructUrl("specimens/${specimenId}/images/data/${specimenImageId}")) {
                contentType(ContentType.Application.Json)
            }
        }
    }
}