package com.vci.vectorcamapp.surveillance_form.domain.use_cases

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.surveillance_form.domain.util.FormValidationError
import javax.inject.Inject

class ValidateLlinTypeUseCase @Inject constructor() {
    operator fun invoke(llinType: String) : Result<Unit, FormValidationError> {
        return if (llinType.isBlank()) {
            Result.Error(FormValidationError.BLANK_LLIN_TYPE)
        } else {
            Result.Success(Unit)
        }
    }
}
