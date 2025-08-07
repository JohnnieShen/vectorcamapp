package com.vci.vectorcamapp.imaging.domain.use_cases

import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.imaging.domain.util.ImagingError
import javax.inject.Inject

class ValidateSpecimenIdUseCase @Inject constructor() {

    companion object {
        private const val SPECIMEN_ID_LENGTH = 6
        private val SPECIMEN_ID_PATTERN = Regex("^[A-Z]{3}\\d{3}$")

        private val LETTER_CORRECTIONS = mapOf(
            "0" to "O",
            "1" to "I",
            "2" to "Z",
            "3" to "E",
            "4" to "A",
            "5" to "S",
            "6" to "G",
            "7" to "T",
            "8" to "B",
            "9" to "P"
        )

        private val DIGIT_CORRECTIONS = mapOf(
            "O" to "0",
            "I" to "1",
            "Z" to "2",
            "E" to "3",
            "A" to "4",
            "S" to "5",
            "G" to "6",
            "T" to "7",
            "B" to "8",
            "P" to "9"
        )
    }

    operator fun invoke(
        specimenId: String, shouldAutoCorrect: Boolean
    ): Result<String, ImagingError> {
        val cleanedSpecimenId = specimenId.trim().replace(" ", "").uppercase()

        if (cleanedSpecimenId.length != SPECIMEN_ID_LENGTH || !cleanedSpecimenId.all { it.isLetterOrDigit() }) {
            return Result.Error(ImagingError.INVALID_SPECIMEN_ID)
        }

        val corrected = if (shouldAutoCorrect) {
            buildString {
                cleanedSpecimenId.forEachIndexed { index, character ->
                    val correctedChar = if (index < 3) {
                        LETTER_CORRECTIONS.getOrDefault(character.toString(), character)
                    } else {
                        DIGIT_CORRECTIONS.getOrDefault(character.toString(), character)
                    }
                    append(correctedChar)
                }
            }
        } else {
            cleanedSpecimenId
        }

        return if (SPECIMEN_ID_PATTERN.matches(corrected)) {
            Result.Success(corrected)
        } else {
            Result.Error(ImagingError.INVALID_SPECIMEN_ID)
        }
    }
}
