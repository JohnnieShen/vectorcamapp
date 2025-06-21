package com.vci.vectorcamapp.core.domain.util.network

import com.vci.vectorcamapp.core.domain.util.Error

enum class NetworkError : Error {
    // Generic Errors
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN,

    // Endpoint-Specific Errors
    SESSION_NOT_COMPLETED
}
