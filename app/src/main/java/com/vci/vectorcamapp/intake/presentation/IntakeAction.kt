package com.vci.vectorcamapp.intake.presentation

import com.vci.vectorcamapp.intake.domain.model.IntakeDropdownOptions.CollectionMethodOption
import com.vci.vectorcamapp.intake.domain.model.IntakeDropdownOptions.LlinBrandOption
import com.vci.vectorcamapp.intake.domain.model.IntakeDropdownOptions.LlinTypeOption
import com.vci.vectorcamapp.intake.domain.model.IntakeDropdownOptions.SpecimenConditionOption

sealed interface IntakeAction {
    data object ReturnToLandingScreen: IntakeAction
    data object SubmitIntakeForm: IntakeAction
    data class EnterCollectorTitle(val text: String) : IntakeAction
    data class EnterCollectorName(val text: String) : IntakeAction
    data class SelectDistrict(val district: String) : IntakeAction
    data class SelectSentinelSite(val sentinelSite: String) : IntakeAction
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
