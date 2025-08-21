package com.vci.vectorcamapp.core.domain.model

data class SurveillanceForm(
    val numPeopleSleptInHouse: Int,
    val wasIrsConducted: Boolean,
    val monthsSinceIrs: Int?,
    val numLlinsAvailable: Int,
    val llinType: String?,
    val llinBrand: String?,
    val numPeopleSleptUnderLlin: Int?,
    val submittedAt: Long?,
)
