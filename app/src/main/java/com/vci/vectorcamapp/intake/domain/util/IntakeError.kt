package com.vci.vectorcamapp.intake.domain.util

import com.vci.vectorcamapp.core.domain.util.Error

enum class IntakeError : Error {
    SITE_NOT_FOUND,
    PROGRAM_NOT_FOUND,
    LOCATION_PERMISSION_DENIED,
    LOCATION_GPS_TIMEOUT,
    UNKNOWN_ERROR;
}
