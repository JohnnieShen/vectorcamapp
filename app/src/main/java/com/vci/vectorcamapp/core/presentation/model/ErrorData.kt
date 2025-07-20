package com.vci.vectorcamapp.core.presentation.model

import androidx.compose.material3.SnackbarDuration
import com.vci.vectorcamapp.core.domain.util.Error

data class ErrorData(
    val error: Error,
    val duration: SnackbarDuration = SnackbarDuration.Long
)
