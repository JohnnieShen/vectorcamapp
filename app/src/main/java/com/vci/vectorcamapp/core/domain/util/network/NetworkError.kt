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
    SERIALIZATION,
    UNKNOWN,

    // Endpoint-Specific Errors
    SESSION_NOT_COMPLETED
}
