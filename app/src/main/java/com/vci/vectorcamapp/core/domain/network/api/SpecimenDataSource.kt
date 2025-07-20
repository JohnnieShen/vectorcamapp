package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.specimen.PostSpecimenResponseDto
import com.vci.vectorcamapp.core.data.dto.specimen.SpecimenDto
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface SpecimenDataSource {
    suspend fun postSpecimen(
        specimen: Specimen, inferenceResult: InferenceResult, sessionId: Int
    ): Result<PostSpecimenResponseDto, NetworkError>
    suspend fun getSpecimenById(specimenId: String): Result<SpecimenDto, NetworkError>
}
