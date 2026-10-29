package com.example.focusmate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = CreamBackground,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBrown,
    onSecondary = Color.White,
    secondaryContainer = CreamCard,
    onSecondaryContainer = TextPrimary,
    tertiary = PriorityMedium,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CreamSurface,
    onSurface = TextPrimary,
    surfaceVariant = CreamCard,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = PriorityHigh,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = Color.White,
    secondary = AccentBrownLight,
    onSecondary = Color.White,
    secondaryContainer = AccentBrownDark,
    onSecondaryContainer = Color.White,
    tertiary = PriorityMedium,
    onTertiary = Color.White,
    background = Color(0xFF1A1A1A),
    onBackground = Color.White,
    surface = Color(0xFF2A2A2A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF444444),
    error = Color(0xFFCF6679),
    onError = Color.Black,
)

@Composable
fun FocusMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}