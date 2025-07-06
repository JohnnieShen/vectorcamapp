package com.vci.vectorcamapp.core.presentation.util.error

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.SharedFlow

val LocalErrorDataFlow = staticCompositionLocalOf<SharedFlow<ErrorData>> {
    error("No ErrorDataFlow provided")
}
