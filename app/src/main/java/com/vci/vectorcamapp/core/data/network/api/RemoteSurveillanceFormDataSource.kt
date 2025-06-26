package com.vci.vectorcamapp.core.data.network.api

import com.vci.vectorcamapp.core.data.dto.surveillance_form.PostSurveillanceFormRequestDto
import com.vci.vectorcamapp.core.data.dto.surveillance_form.PostSurveillanceFormResponseDto
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.network.safeCall
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.network.api.SurveillanceFormDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class RemoteSurveillanceFormDataSource @Inject constructor(
    private val httpClient: HttpClient
) : SurveillanceFormDataSource {

    override suspend fun postSurveillanceForm(
        surveillanceForm: SurveillanceForm,
        sessionId: Int
    ): Result<PostSurveillanceFormResponseDto, NetworkError> {
        return safeCall<PostSurveillanceFormResponseDto> {
            httpClient.post(constructUrl("sessions/$sessionId/survey")) {
                contentType(ContentType.Application.Json)
                setBody(
                    PostSurveillanceFormRequestDto(
                        sessionId = sessionId,
                        numPeopleSleptInHouse = surveillanceForm.numPeopleSleptInHouse,
                        wasIrsConducted = surveillanceForm.wasIrsConducted,
                        monthsSinceIrs = surveillanceForm.monthsSinceIrs,
                        numLlinsAvailable = surveillanceForm.numLlinsAvailable,
                        llinType = surveillanceForm.llinType,
                        llinBrand = surveillanceForm.llinBrand,
                        numPeopleSleptUnderLlin = surveillanceForm.numPeopleSleptUnderLlin
                    )
                )
            }
        }
    }
}
