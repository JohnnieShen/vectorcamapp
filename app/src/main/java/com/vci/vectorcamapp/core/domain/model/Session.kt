package com.vci.vectorcamapp.core.domain.model

import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import java.util.UUID

data class Session(
    val localId: UUID,
    val remoteId: Int?,
    val houseNumber: String,
    val collectorTitle: String,
    val collectorName: String,
    val collectionDate: Long,
    val collectionMethod: String,
    val specimenCondition: String,
    val createdAt: Long,
    val completedAt: Long?,
    val submittedAt: Long?,
    val notes: String,
    val latitude: Float?,
    val longitude: Float?,
    val type: SessionType
)
