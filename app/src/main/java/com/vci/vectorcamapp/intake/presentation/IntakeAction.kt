package com.vci.vectorcamapp.intake.presentation

import com.vci.vectorcamapp.intake.domain.enums.CollectionMethodOption
import com.vci.vectorcamapp.intake.domain.enums.DistrictOption
import com.vci.vectorcamapp.intake.domain.enums.LlinBrandOption
import com.vci.vectorcamapp.intake.domain.enums.LlinTypeOption
import com.vci.vectorcamapp.intake.domain.enums.SentinelSiteOption
import com.vci.vectorcamapp.intake.domain.enums.SpecimenConditionOption

sealed interface IntakeAction {
    data object ReturnToLandingScreen: IntakeAction
    data object SubmitIntakeForm: IntakeAction
    data class EnterCollectorTitle(val text: String) : IntakeAction
    data class EnterCollectorName(val text: String) : IntakeAction
    data class SelectDistrict(val option: DistrictOption) : IntakeAction
    data class SelectSentinelSite(val option: SentinelSiteOption) : IntakeAction
    data class EnterHouseNumber(val text: String) : IntakeAction
    data class EnterNumPeopleSleptInHouse(val count: String) : IntakeAction
    data class ToggleIrsConducted(val isChecked : Boolean) : IntakeAction
    data class EnterMonthsSinceIrs(val count: String) : IntakeAction
    data class EnterNumLlinsAvailable(val count: String) : IntakeAction
    data class SelectLlinType(val option: LlinTypeOption) : IntakeAction
    data class SelectLlinBrand(val option: LlinBrandOption) : IntakeAction
    data class EnterNumPeopleSleptUnderLlin(val count: String) : IntakeAction
    data class PickCollectionDate(val date: Long) : IntakeAction
    data class SelectCollectionMethod(val option: CollectionMethodOption) : IntakeAction
    data class SelectSpecimenCondition(val option: SpecimenConditionOption) : IntakeAction
    data class EnterNotes(val text: String) : IntakeAction
    data object RetryLocation: IntakeAction
}
