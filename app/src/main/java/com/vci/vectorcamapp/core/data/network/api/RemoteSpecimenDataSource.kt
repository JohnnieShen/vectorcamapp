package com.vci.vectorcamapp.core.data.network.api

import com.vci.vectorcamapp.core.data.dto.inference_result.InferenceResultDto
import com.vci.vectorcamapp.core.data.dto.specimen.PostSpecimenRequestDto
import com.vci.vectorcamapp.core.data.dto.specimen.PostSpecimenResponseDto
import com.vci.vectorcamapp.core.data.dto.specimen.SpecimenDto
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.network.safeCall
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.network.api.SpecimenDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class RemoteSpecimenDataSource @Inject constructor(
    private val httpClient: HttpClient
) : SpecimenDataSource {

    override suspend fun postSpecimen(
        specimen: Specimen, inferenceResult: InferenceResult, sessionId: Int
    ): Result<PostSpecimenResponseDto, NetworkError> {
        return safeCall<PostSpecimenResponseDto> {
            httpClient.post(constructUrl("specimens")) {
                contentType(ContentType.Application.Json)
                setBody(
                    PostSpecimenRequestDto(
                        specimenId = specimen.id,
                        sessionId = sessionId,
                        species = specimen.species,
                        sex = specimen.sex,
                        abdomenStatus = specimen.abdomenStatus,
                        capturedAt = specimen.capturedAt,
                        inferenceResult = InferenceResultDto(
                            bboxTopLeftX = inferenceResult.bboxTopLeftX,
                            bboxTopLeftY = inferenceResult.bboxTopLeftY,
                            bboxWidth = inferenceResult.bboxWidth,
                            bboxHeight = inferenceResult.bboxHeight,
                            bboxConfidence = inferenceResult.bboxConfidence,
                            bboxClassId = inferenceResult.bboxClassId,
                            speciesProbabilities = inferenceResult.speciesLogits,
                            sexProbabilities = inferenceResult.sexLogits,
                            abdomenStatusProbabilities = inferenceResult.abdomenStatusLogits
                        )
                    )
                )
            }
        }
    }

    override suspend fun getSpecimenById(specimenId: String): Result<SpecimenDto, NetworkError> {
        return safeCall<SpecimenDto> {
            httpClient.get(constructUrl("specimens/$specimenId")) {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
