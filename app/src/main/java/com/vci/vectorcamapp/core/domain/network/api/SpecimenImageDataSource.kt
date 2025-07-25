package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.specimen_image.PostSpecimenImageResponseDto
import com.vci.vectorcamapp.core.data.dto.specimen_image.SpecimenImageDto
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface SpecimenImageDataSource {
    suspend fun postSpecimenImageMetadata(
        specimenImage: SpecimenImage, inferenceResult: InferenceResult, specimenId: String
    ): Result<PostSpecimenImageResponseDto, NetworkError>

    suspend fun getSpecimenImageMetadata(specimenImageId: Int, specimenId: String): Result<SpecimenImageDto, NetworkError>
}
