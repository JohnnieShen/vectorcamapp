package com.vci.vectorcamapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.vci.vectorcamapp.R

val displayLargeSize = 28.sp
val displayMediumSize = 24.sp
val displaySmallSize = 22.sp
val headlineLargeSize = 20.sp
val headlineMediumSize = 18.sp
val headlineSmallSize = 17.sp
val titleLargeSize = 16.sp
val titleMediumSize = 15.sp
val titleSmallSize = 14.sp
val bodyLargeSize = 16.sp
val bodyMediumSize = 14.sp
val bodySmallSize = 12.sp
val labelLargeSize = 12.sp
val labelMediumSize = 11.sp
val labelSmallSize = 9.sp

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),
        fontProvider = provider,
    )
)

val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),
        fontProvider = provider,
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontFamily = displayFontFamily,
        fontSize = displayLargeSize,
        lineHeight = displayLargeSize,
        fontWeight = FontWeight.Black
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = displayFontFamily,
        fontSize = displayMediumSize,
        lineHeight = displayMediumSize,
        fontWeight = FontWeight.ExtraBold
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = displayFontFamily,
        fontSize = displaySmallSize,
        lineHeight = displaySmallSize,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = displayFontFamily,
        fontSize = headlineLargeSize,
        lineHeight = headlineLargeSize,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = displayFontFamily,
        fontSize = headlineMediumSize,
        lineHeight = headlineMediumSize,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = displayFontFamily,
        fontSize = headlineSmallSize,
        lineHeight = headlineSmallSize,
        fontWeight = FontWeight.Medium
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = displayFontFamily,
        fontSize = titleLargeSize,
        lineHeight = titleLargeSize,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = displayFontFamily,
        fontSize = titleMediumSize,
        lineHeight = titleMediumSize,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = displayFontFamily,
        fontSize = titleSmallSize,
        lineHeight = titleSmallSize,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = bodyFontFamily,
        fontSize = bodyLargeSize,
        lineHeight = bodyLargeSize,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = bodyFontFamily,
        fontSize = bodyMediumSize,
        lineHeight = bodyMediumSize,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = baseline.bodySmall.copy(
        fontFamily = bodyFontFamily,
        fontSize = bodySmallSize,
        lineHeight = bodySmallSize,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = baseline.labelLarge.copy(
        fontFamily = bodyFontFamily,
        fontSize = labelLargeSize,
        lineHeight = labelLargeSize,
        fontWeight = FontWeight.Light
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = bodyFontFamily,
        fontSize = labelMediumSize,
        lineHeight = labelMediumSize,
        fontWeight = FontWeight.Light
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = bodyFontFamily,
        fontSize = labelSmallSize,
        lineHeight = labelSmallSize,
        fontWeight = FontWeight.Light
    ),
)
