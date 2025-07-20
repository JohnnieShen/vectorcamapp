package com.vci.vectorcamapp.intake.domain.model

object IntakeDropdownOptions {

    enum class CollectionMethodOption(val label: String) {
        CDC_LIGHT_TRAP("CDC Light Trap (LTC)"),
        PYRETHRUM_SPRAY_CATCH("Pyrethrum Spray Catch (PSC)"),
        HUMAN_LANDING_CATCH("Human Landing Catch (HLC)"),
        OTHER("Other")
    }

    enum class LlinBrandOption(val label: String) {
        OLYSET_NET("OLYSET Net"),
        OLYSET_PLUS("OLYSET Plus"),
        INTERCEPTOR("Interceptor"),
        INTERCEPTOR_G2("Interceptor G2"),
        ROYAL_SENTRY("Royal Sentry"),
        ROYAL_SENTRY_2("Royal Sentry 2.0"),
        ROYAL_GUARD("Royal Guard"),
        PERMANET_2("PermaNet 2.0"),
        PERMANET_3("PermaNet 3.0"),
        DURANET_LLIN("Duranet LLIN"),
        MIRANET("MiraNet"),
        MAGNET("MAGNet"),
        VEERALIN("VEERALIN"),
        YAHE_LN("Yahe LN"),
        SAFENET("SafeNet"),
        YORKOOL_LN("Yorkool LN"),
        PANDA_NET_2("Panda Net 2.0"),
        TSARA_BOOST("Tsara Boost"),
        TSARA_SOFT("Tsara Soft"),
        TSARA_PLUS("Tsara Plus"),
        OTHER("Other")
    }

    enum class LlinTypeOption(val label: String) {
        PYRETHROID_ONLY("Pyrethroid Only"),
        PYRETHROID_PBO("Pyrethroid + PBO"),
        PYRETHROID_CHLORFENAPYR("Pyrethroid + Chlorfenapyr"),
        PYRETHROID_PYRIPROXYFEN("Pyrethroid + Pyriproxyfen"),
        OTHER("Other")
    }

    enum class SpecimenConditionOption(val label: String) {
        FRESH("Fresh"),
        DESSICATED("Dessicated"),
        OTHER("Other")
    }
}