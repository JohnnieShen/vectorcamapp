package com.vci.vectorcamapp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class Colors(
    val primary: Color = Color(0xFF6DCB81),
    val secondary: Color = Color(0xFF4DBD8C),
    val accent: Color = Color(0xFFD1FAE5),
    val headerGradientTopLeft: Color = Color(0xFFB6EAD8),
    val headerGradientBottomRight: Color = Color(0xFFD3F5DF),
    val buttonGradientLeft: Color = Color(0xFF57C88B),
    val buttonGradientRight: Color = Color(0xFF6EDB81),
    val buttonText: Color = Color(0xFFFFFFFF),
    val disabled: Color = Color(0xFF828282),
    val successConfirm: Color = Color(0xFF43A047),
    val warning: Color = Color(0xFFFFCA1B),
    val error: Color = Color(0xFFE54848),
    val textPrimary: Color = Color(0xFF1A1A1A),
    val textSecondary: Color = Color(0xFF64748B),
    val fieldBorder: Color = Color(0xFFD9D9D9),
    val divider: Color = Color(0xFFD5D5D5),
    val cardGlow: Color = Color(0xFF10B981),
    val iconBackground: Color = Color(0xFFE0F2F1),
    val icon: Color = Color(0xFF00796B),
    val pillBackground: Color = Color(0xFFC8EBFF),
    val pillText: Color = Color(0xFF0277BD),
    val segmentedTabBarActiveGradientLeft: Color = Color(0xFF34D399),
    val segmentedTabBarActiveGradientRight: Color = Color(0xFF10B981),
    val segmentedTabBarInactive: Color = Color(0xFFF2FDF5),
    val segmentedTabBarActiveText: Color = Color(0xFFFFFFFF),
    val segmentedTabBarInactiveText: Color = Color(0xFF64748B),
    val cardBackground: Color = Color(0xFFFFFFFF),
    val appBackground: Color = Color(0xFFF7F8F7),
    val transparent: Color = Color(0x00000000),
)

val LocalColors = staticCompositionLocalOf { Colors() }
