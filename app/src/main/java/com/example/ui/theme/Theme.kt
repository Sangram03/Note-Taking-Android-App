package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = DeepSlateBg,
    primaryContainer = SolidIndigo,
    onPrimaryContainer = Color.White,
    secondary = SolidIndigo,
    onSecondary = Color.White,
    tertiary = AccentMint,
    onTertiary = DeepSlateBg,
    background = DeepSlateBg,
    onBackground = TextPrimary,
    surface = CardSlate,
    onSurface = TextPrimary,
    surfaceVariant = CardSlateVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSlate
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE5E7EB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We default to dark theme for maximum cosmic contrast
    dynamicColor: Boolean = false, // Use our gorgeous custom colors instead of wallpaper extraction
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
