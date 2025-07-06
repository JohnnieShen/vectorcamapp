package com.vci.vectorcamapp.complete_session.domain.util

import com.vci.vectorcamapp.core.domain.util.Error

enum class CompleteSessionError : Error {
    SESSION_NOT_FOUND,
    SESSION_OR_SITE_NOT_FOUND,
    SESSION_OR_SURVEILLANCE_FORM_NOT_FOUND,
    UNKNOWN_ERROR;
}
