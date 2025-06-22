package com.vci.vectorcamapp.surveillance_form.domain.enums

enum class LlinTypeOption(override val label: String) : DropdownOption {
    PYRETHROID_ONLY("Pyrethroid Only"),
    PYRETHROID_PBO("Pyrethroid + PBO"),
    PYRETHROID_CHLORFENAPYR("Pyrethroid + Chlorfenapyr"),
    PYRETHROID_PYRIPROXYFEN("Pyrethroid + Pyriproxyfen"),
    OTHER("Other")
}
