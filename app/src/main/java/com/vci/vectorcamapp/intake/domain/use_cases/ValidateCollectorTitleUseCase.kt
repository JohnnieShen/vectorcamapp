package com.vci.vectorcamapp.intake.domain.use_cases

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.intake.domain.util.FormValidationError
import javax.inject.Inject

class ValidateCollectorTitleUseCase @Inject constructor() {
    operator fun invoke(collectorTitle: String) : Result<Unit, FormValidationError> {
        return if (collectorTitle.isBlank()) {
            Result.Error(FormValidationError.BLANK_COLLECTOR_TITLE)
        } else {
            Result.Success(Unit)
        }
    }
}
