package com.vci.vectorcamapp.surveillance_form.location.data.repository

import android.location.Location
import com.vci.vectorcamapp.surveillance_form.location.data.LocationClient
import com.vci.vectorcamapp.surveillance_form.location.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImplementation @Inject constructor(
    private val locationClient: LocationClient
) : LocationRepository {
    override suspend fun getCurrentLocation(): Location =
        locationClient.getCurrentLocation()
}
