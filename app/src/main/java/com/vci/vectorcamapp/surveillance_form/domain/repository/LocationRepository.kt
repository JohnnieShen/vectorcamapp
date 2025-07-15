package com.vci.vectorcamapp.surveillance_form.domain.repository

import android.location.Location
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.surveillance_form.domain.util.SurveillanceFormError

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location, SurveillanceFormError>
}
