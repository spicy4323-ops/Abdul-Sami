package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AyeshaColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = VoidBlack,
    primaryContainer = GlassSurfaceElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = CyberViolet,
    onSecondary = VoidBlack,
    secondaryContainer = GlassSurfaceElevated,
    onSecondaryContainer = SoftViolet,
    tertiary = CyanGlow,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassSurfaceElevated,
    error = RubyAlert,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AyeshaColorScheme,
        typography = Typography,
        content = content
    )
}
