package com.vci.vectorcamapp.intake.domain.enums

enum class SpecimenConditionOption(override val label: String) : DropdownOption {
    FRESH("Fresh"),
    DESSICATED("Dessicated"),
    OTHER("Other")
}
