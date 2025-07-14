package com.vci.vectorcamapp.complete_session.details.domain.util

import com.vci.vectorcamapp.core.domain.util.Error

enum class CompleteSessionDetailsError : Error {
    SESSION_NOT_FOUND,
    SITE_NOT_FOUND,
    SURVEILLANCE_FORM_NOT_FOUND,
    SPECIMENS_NOT_FOUND,
    UNKNOWN_ERROR;
}
