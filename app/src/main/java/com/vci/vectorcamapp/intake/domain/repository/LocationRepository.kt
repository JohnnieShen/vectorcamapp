package com.vci.vectorcamapp.intake.domain.repository

import android.location.Location
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.intake.domain.util.IntakeError

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location, IntakeError>
}
