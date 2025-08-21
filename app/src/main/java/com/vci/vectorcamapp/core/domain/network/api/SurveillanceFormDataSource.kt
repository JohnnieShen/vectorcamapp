package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.surveillance_form.PostSurveillanceFormResponseDto
import com.vci.vectorcamapp.core.data.dto.surveillance_form.SurveillanceFormDto
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface SurveillanceFormDataSource {
    suspend fun postSurveillanceForm(
        surveillanceForm: SurveillanceForm, sessionId: Int
    ): Result<PostSurveillanceFormResponseDto, NetworkError>
    suspend fun getSurveillanceFormBySessionId(sessionId: Int): Result<SurveillanceFormDto, NetworkError>
}
