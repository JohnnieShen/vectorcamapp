package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.session.PostSessionResponseDto
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface SessionDataSource {
    suspend fun postSession(session: Session, siteId: Int, deviceId: Int): Result<PostSessionResponseDto, NetworkError>
}
