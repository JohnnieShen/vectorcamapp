package com.vci.vectorcamapp.core.data.dto.surveillance_form

import kotlinx.serialization.Serializable

@Serializable
data class SurveillanceFormDto(
    val sessionId: Int = 0,
    val numPeopleSleptInHouse: Int = 0,
    val wasIrsConducted: Boolean = false,
    val monthsSinceIrs: Int? = null,
    val numLlinsAvailable: Int = 0,
    val llinType: String? = null,
    val llinBrand: String? = null,
    val numPeopleSleptUnderLlin: Int? = null,
    val submittedAt: Long? = null,
)
