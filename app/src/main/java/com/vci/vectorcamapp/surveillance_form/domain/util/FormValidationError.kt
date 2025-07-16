package com.vci.vectorcamapp.surveillance_form.domain.util

import com.vci.vectorcamapp.core.domain.util.Error

enum class FormValidationError : Error {
    BLANK_COLLECTOR_TITLE,
    BLANK_COLLECTOR_NAME,
    BLANK_DISTRICT,
    BLANK_SENTINEL_SITE,
    BLANK_HOUSE_NUMBER,
    BLANK_LLIN_TYPE,
    BLANK_LLIN_BRAND,
    FUTURE_COLLECTION_DATE,
    BLANK_COLLECTION_METHOD,
    BLANK_SPECIMEN_CONDITION;
}
