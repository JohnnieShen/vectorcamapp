package com.vci.vectorcamapp.core.presentation.components.empty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun EmptySpace(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = width, height = height))
}
