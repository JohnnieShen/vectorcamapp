package com.vci.vectorcamapp.intake.presentation.model

import com.vci.vectorcamapp.intake.domain.util.FormValidationError

data class IntakeErrors(
    val collectorTitle: FormValidationError?,
    val collectorName: FormValidationError?,
    val district: FormValidationError?,
    val sentinelSite: FormValidationError?,
    val houseNumber: FormValidationError?,
    val llinType: FormValidationError?,
    val llinBrand: FormValidationError?,
    val collectionDate: FormValidationError?,
    val collectionMethod: FormValidationError?,
    val specimenCondition: FormValidationError?
)
