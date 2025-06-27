package com.vci.vectorcamapp.surveillance_form.location.data

import com.vci.vectorcamapp.core.domain.util.Error

enum class LocationError : Error {
    PERMISSION_DENIED, GPS_TIMEOUT, UNKNOWN,
}
