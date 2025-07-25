package com.vci.vectorcamapp.core.data.network.api

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
        specimenImage: SpecimenImage,
        inferenceResult: InferenceResult,
        specimenId: String
    ): Result<PostSpecimenImageResponseDto, NetworkError> {
        return safeCall<PostSpecimenImageResponseDto> {
            httpClient.post(constructUrl("specimens/${specimenId}/images/data")) {
                contentType(ContentType.Application.Json)
                setBody(
                    PostSpecimenImageRequestDto(
                        species = specimenImage.species,
                        sex = specimenImage.sex,
                        abdomenStatus = specimenImage.abdomenStatus,
                        capturedAt = specimenImage.capturedAt,
                        inferenceResult = InferenceResultDto(
                            bboxTopLeftX = inferenceResult.bboxTopLeftX,
                            bboxTopLeftY = inferenceResult.bboxTopLeftY,
                            bboxWidth = inferenceResult.bboxWidth,
                            bboxHeight = inferenceResult.bboxHeight,
                            bboxConfidence = inferenceResult.bboxConfidence,
                            bboxClassId = inferenceResult.bboxClassId,
                            speciesLogits = inferenceResult.speciesLogits,
                            sexLogits = inferenceResult.sexLogits,
                            abdomenStatusLogits = inferenceResult.abdomenStatusLogits
                        )
                    )
                )
            }
        }
    }

    override suspend fun getSpecimenImageMetadata(specimenImageId: Int, specimenId: String): Result<SpecimenImageDto, NetworkError> {
        return safeCall<SpecimenImageDto> {
            httpClient.get(constructUrl("specimens/${specimenId}/images/data/${specimenImageId}")) {
                contentType(ContentType.Application.Json)
            }
        }
    }
}