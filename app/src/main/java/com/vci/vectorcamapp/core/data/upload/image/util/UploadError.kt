package com.vci.vectorcamapp.core.data.upload.image.util

import com.vci.vectorcamapp.core.domain.util.Error

enum class UploadError : Error {
    NO_INTERNET,
    TIMEOUT,
    VERIFICATION_ERROR,
    PROTOCOL_ERROR,
    UNKNOWN_ERROR
}