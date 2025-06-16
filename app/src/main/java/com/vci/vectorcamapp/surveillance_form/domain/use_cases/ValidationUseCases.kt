package com.vci.vectorcamapp.surveillance_form.domain.use_cases

import javax.inject.Inject

data class ValidationUseCases @Inject constructor(
    val validateCollectorTitle: ValidateCollectorTitleUseCase,
    val validateCollectorName: ValidateCollectorNameUseCase,
    val validateDistrict: ValidateDistrictUseCase,
    val validateSentinelSite: ValidateSentinelSiteUseCase,
    val validateHouseNumber: ValidateHouseNumberUseCase,
    val validateLlinType: ValidateLlinTypeUseCase,
    val validateLlinBrand: ValidateLlinBrandUseCase,
    val validateCollectionDate: ValidateCollectionDateUseCase,
    val validateCollectionMethod: ValidateCollectionMethodUseCase,
    val validateSpecimenCondition: ValidateSpecimenConditionUseCase
)
