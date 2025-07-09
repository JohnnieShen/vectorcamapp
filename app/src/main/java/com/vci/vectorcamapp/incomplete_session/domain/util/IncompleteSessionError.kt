package com.vci.vectorcamapp.incomplete_session.domain.util

import com.vci.vectorcamapp.core.domain.util.Error

enum class IncompleteSessionError : Error {
    SESSION_NOT_FOUND,
    SESSION_RETRIEVAL_FAILED,
    UNKNOWN_ERROR;
}
