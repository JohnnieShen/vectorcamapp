package com.vci.vectorcamapp.surveillance_form.domain.enums

enum class SpecimenConditionOption(override val label: String) : SurveillanceFormDropdownOption {
    FRESH("Fresh"),
    DESSICATED("Dessicated"),
    OTHER("Other")
}
