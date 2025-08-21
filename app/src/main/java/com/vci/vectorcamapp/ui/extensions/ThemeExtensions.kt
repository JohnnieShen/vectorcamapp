package com.vci.vectorcamapp.ui.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.vci.vectorcamapp.ui.theme.Colors
import com.vci.vectorcamapp.ui.theme.Dimensions
import com.vci.vectorcamapp.ui.theme.LocalColors
import com.vci.vectorcamapp.ui.theme.LocalDimensions

val MaterialTheme.dimensions: Dimensions
    @Composable
    get() = LocalDimensions.current

val MaterialTheme.colors: Colors
    @Composable
    get() = LocalColors.current