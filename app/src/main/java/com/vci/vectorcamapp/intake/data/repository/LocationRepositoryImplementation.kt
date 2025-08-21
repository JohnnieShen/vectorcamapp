package com.vci.vectorcamapp.intake.data.repository

import android.location.Location
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.intake.data.LocationClient
import com.vci.vectorcamapp.intake.domain.repository.LocationRepository
import com.vci.vectorcamapp.intake.domain.util.IntakeError
import javax.inject.Inject

class LocationRepositoryImplementation @Inject constructor(
    private val locationClient: LocationClient
) : LocationRepository {
    override suspend fun getCurrentLocation(): Result<Location, IntakeError> =
        locationClient.getCurrentLocation()
}
