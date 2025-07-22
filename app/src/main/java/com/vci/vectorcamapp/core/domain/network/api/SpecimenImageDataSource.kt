package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.specimen_image.SpecimenImageDto
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface SpecimenImageDataSource {
    suspend fun checkImageExists(specimenId: String, imageId: Int): Result<SpecimenImageDto, NetworkError>
}