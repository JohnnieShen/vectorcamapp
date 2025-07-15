package com.vci.vectorcamapp.surveillance_form.data.repository

import android.location.Location
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.surveillance_form.data.LocationClient
import com.vci.vectorcamapp.surveillance_form.domain.repository.LocationRepository
import com.vci.vectorcamapp.surveillance_form.domain.util.SurveillanceFormError
import javax.inject.Inject

class LocationRepositoryImplementation @Inject constructor(
    private val locationClient: LocationClient
) : LocationRepository {
    override suspend fun getCurrentLocation(): Result<Location, SurveillanceFormError> =
        locationClient.getCurrentLocation()
}
