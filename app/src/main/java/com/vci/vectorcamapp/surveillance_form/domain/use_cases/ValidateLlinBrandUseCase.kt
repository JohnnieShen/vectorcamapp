package com.vci.vectorcamapp.surveillance_form.domain.use_cases

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.surveillance_form.domain.util.FormValidationError
import javax.inject.Inject

class ValidateLlinBrandUseCase @Inject constructor() {
    operator fun invoke(llinBrand: String) : Result<Unit, FormValidationError> {
        return if (llinBrand.isBlank()) {
            Result.Error(FormValidationError.BLANK_LLIN_BRAND)
        } else {
            Result.Success(Unit)
        }
    }
}
