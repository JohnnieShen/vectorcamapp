package com.vci.vectorcamapp.intake.domain.use_cases

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.intake.domain.util.FormValidationError
import javax.inject.Inject

class ValidateCollectionMethodUseCase @Inject constructor() {
    operator fun invoke(collectionMethod: String): Result<Unit, FormValidationError> {
        return if (collectionMethod.isBlank()) {
            Result.Error(FormValidationError.BLANK_COLLECTION_METHOD)
        } else {
            Result.Success(Unit)
        }
    }
}
