package com.nitin3it.kidsafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Indigo = Color(0xFF5B5BF7)
val Violet = Color(0xFF8E54E9)
val Teal = Color(0xFF00BFA6)
val Coral = Color(0xFFFF7A59)
val Amber = Color(0xFFFFB020)

/** Brand gradient used for headers and hero cards. */
val BrandGradient = Brush.linearGradient(listOf(Indigo, Violet))
val ChildGradient = Brush.linearGradient(listOf(Teal, Color(0xFF2E8BFF)))

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E3FF),
    onPrimaryContainer = Color(0xFF14146B),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF7EF),
    onSecondaryContainer = Color(0xFF00382F),
    tertiary = Coral,
    tertiaryContainer = Color(0xFFFFE2D9),
    onTertiaryContainer = Color(0xFF5C1A08),
    background = Color(0xFFF6F6FB),
    onBackground = Color(0xFF1B1B24),
    surface = Color.White,
    onSurface = Color(0xFF1B1B24),
    surfaceVariant = Color(0xFFEEEEF6),
    onSurfaceVariant = Color(0xFF5E5E70),
    outline = Color(0xFFD5D5E2),
    error = Color(0xFFE5484D),
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF6B0A0D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9A8FF),
    onPrimary = Color(0xFF1E1D7A),
    primaryContainer = Color(0xFF3A39B8),
    onPrimaryContainer = Color(0xFFE4E3FF),
    secondary = Color(0xFF4FE0C6),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF00574A),
    onSecondaryContainer = Color(0xFFCFF7EF),
    tertiary = Color(0xFFFFB59F),
    tertiaryContainer = Color(0xFF7A2E17),
    onTertiaryContainer = Color(0xFFFFE2D9),
    background = Color(0xFF111118),
    onBackground = Color(0xFFE6E5F0),
    surface = Color(0xFF1B1B24),
    onSurface = Color(0xFFE6E5F0),
    surfaceVariant = Color(0xFF2A2A36),
    onSurfaceVariant = Color(0xFFB9B8CB),
    outline = Color(0xFF45455A),
    error = Color(0xFFFF8A8D),
    errorContainer = Color(0xFF5C1517),
    onErrorContainer = Color(0xFFFFDAD9),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
)

@Composable
fun KidSafeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
