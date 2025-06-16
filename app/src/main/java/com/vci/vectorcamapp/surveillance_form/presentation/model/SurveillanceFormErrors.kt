package com.vci.vectorcamapp.surveillance_form.presentation.model

import com.vci.vectorcamapp.surveillance_form.domain.util.FormValidationError

data class SurveillanceFormErrors(
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
