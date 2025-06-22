package com.vci.vectorcamapp.surveillance_form.domain.enums

enum class CollectionMethodOption(override val label: String) : DropdownOption {
    CDC_LIGHT_TRAP("CDC Light Trap (LTC)"),
    PYRETHRUM_SPRAY_CATCH("Pyrethrum Spray Catch (PSC)"),
    HUMAN_LANDING_CATCH("Human Landing Catch (HLC)"),
    OTHER("Other")
}
