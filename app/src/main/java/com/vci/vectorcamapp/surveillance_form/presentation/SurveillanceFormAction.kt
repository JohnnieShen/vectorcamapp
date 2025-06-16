package com.vci.vectorcamapp.surveillance_form.presentation

import com.vci.vectorcamapp.surveillance_form.domain.enums.CollectionMethodOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.DistrictOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.LlinBrandOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.LlinTypeOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.SentinelSiteOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.SpecimenConditionOption

sealed interface SurveillanceFormAction {
    data object SaveSessionProgress: SurveillanceFormAction
    data object SubmitSurveillanceForm: SurveillanceFormAction
    data class EnterCollectorTitle(val text: String) : SurveillanceFormAction
    data class EnterCollectorName(val text: String) : SurveillanceFormAction
    data class SelectDistrict(val option: DistrictOption) : SurveillanceFormAction
    data class SelectSentinelSite(val option: SentinelSiteOption) : SurveillanceFormAction
    data class EnterHouseNumber(val text: String) : SurveillanceFormAction
    data class EnterNumPeopleSleptInHouse(val count: String) : SurveillanceFormAction
    data class ToggleIrsConducted(val isChecked : Boolean) : SurveillanceFormAction
    data class EnterMonthsSinceIrs(val count: String) : SurveillanceFormAction
    data class EnterNumLlinsAvailable(val count: String) : SurveillanceFormAction
    data class SelectLlinType(val option: LlinTypeOption) : SurveillanceFormAction
    data class SelectLlinBrand(val option: LlinBrandOption) : SurveillanceFormAction
    data class EnterNumPeopleSleptUnderLlin(val count: String) : SurveillanceFormAction
    data class PickCollectionDate(val date: Long) : SurveillanceFormAction
    data class SelectCollectionMethod(val option: CollectionMethodOption) : SurveillanceFormAction
    data class SelectSpecimenCondition(val option: SpecimenConditionOption) : SurveillanceFormAction
    data class EnterNotes(val text: String) : SurveillanceFormAction
}
