package com.vci.vectorcamapp.surveillance_form.domain.use_cases

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.surveillance_form.domain.util.FormValidationError
import javax.inject.Inject

class ValidateDistrictUseCase @Inject constructor() {
    operator fun invoke(district: String) : Result<Unit, FormValidationError> {
        return if (district.isBlank()) {
            Result.Error(FormValidationError.BLANK_DISTRICT)
        } else {
            Result.Success(Unit)
        }
    }
}
