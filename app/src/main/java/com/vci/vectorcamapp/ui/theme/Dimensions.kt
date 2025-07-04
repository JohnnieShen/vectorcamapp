package com.vci.vectorcamapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    // Padding & Spacing
    val paddingExtraSmall: Dp = 4.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val paddingExtraLarge: Dp = 32.dp,
    val spacingExtraSmall: Dp = 2.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingMedium: Dp = 16.dp,
    val spacingLarge: Dp = 32.dp,

    // Component Heights
    val componentHeightSmall: Dp = 36.dp,
    val componentHeightMedium: Dp = 48.dp,
    val componentHeightLarge: Dp = 60.dp,
    val componentHeightExtraLarge: Dp = 72.dp,

    // Icon Sizes
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp,

    // Corner Radii
    val cornerRadiusSmall: Dp = 8.dp,
    val cornerRadiusMedium: Dp = 16.dp,
    val cornerRadiusLarge: Dp = 24.dp,

    // Elevation / Shadows
    val shadowOffsetExtraSmall: Dp = 1.dp,
    val shadowOffsetSmall: Dp = 2.dp,
    val shadowOffsetMedium: Dp = 4.dp,
    val shadowOffsetLarge: Dp = 8.dp,
    val shadowBlurSmall: Dp = 4.dp,
    val shadowBlurMedium: Dp = 8.dp,
    val shadowBlurLarge: Dp = 16.dp,
    val shadowSpreadSmall: Dp = 2.dp,
    val shadowSpreadMedium: Dp = 4.dp,
    val shadowSpreadLarge: Dp = 6.dp,
    val elevationSmall: Dp = 2.dp,
    val elevationMedium: Dp = 4.dp,
    val elevationLarge: Dp = 8.dp,

    // Divider / Border
    val dividerThickness: Dp = 2.dp,
    val borderThicknessThin: Dp = 1.dp,
    val borderThicknessThick: Dp = 2.dp,

    // Typography-related spacing
    val lineHeightSmall: Dp = 16.dp,
    val lineHeightMedium: Dp = 24.dp,
    val lineHeightLarge: Dp = 32.dp,
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }

@Composable
fun screenWidthFraction(fraction: Float): Dp {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / displayMetrics.density
    return remember(fraction, screenWidthDp) { (screenWidthDp * fraction).dp }
}

@Composable
fun screenHeightFraction(fraction: Float): Dp {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / displayMetrics.density
    return remember(fraction, screenHeightDp) { (screenHeightDp * fraction).dp }
}
