package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.specimen.PostSpecimenResponseDto
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface SpecimenDataSource {
    suspend fun postSpecimen(
        specimen: Specimen, boundingBox: BoundingBox, sessionId: Int
    ): Result<PostSpecimenResponseDto, NetworkError>
}
