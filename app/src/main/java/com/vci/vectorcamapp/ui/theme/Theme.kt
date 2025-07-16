package com.vci.vectorcamapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun VectorcamappTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColors provides Colors(), LocalDimensions provides Dimensions()) {
        MaterialTheme(
            typography = AppTypography,
            content = content
        )
    }
}
