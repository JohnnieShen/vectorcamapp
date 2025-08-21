package com.vci.vectorcamapp.core.domain.util.network

import com.vci.vectorcamapp.core.domain.util.Error

enum class NetworkError : Error {
    // Generic Errors
    REQUEST_TIMEOUT,
    NOT_FOUND,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    CONFLICT,
    CLIENT_ERROR,
    SERVER_ERROR,
    SERIALIZATION_ERROR,
    UNKNOWN_ERROR,

    // Endpoint-Specific Errors
    SESSION_NOT_COMPLETED,
    TUS_TRANSIENT_ERROR,
    TUS_PERMANENT_ERROR;
}
