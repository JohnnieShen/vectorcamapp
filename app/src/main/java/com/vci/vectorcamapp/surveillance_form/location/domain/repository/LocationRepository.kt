package com.vci.vectorcamapp.surveillance_form.location.domain.repository

import android.location.Location

interface LocationRepository {
    suspend fun getCurrentLocation(): Location
}
