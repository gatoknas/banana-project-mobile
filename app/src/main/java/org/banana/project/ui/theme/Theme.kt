package org.banana.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3)
)

val ColorScheme.retroShadow: Color
    get() = if (primary == TechniColors.Crimson) Color.Black else TechniColors.TokyoNightMagenta

val ColorScheme.retroOutline: Color
    get() = if (primary == TechniColors.Crimson) Color.Black else outline

private val LightColorScheme = lightColorScheme(
    primary = TechniColors.Crimson,
    secondary = TechniColors.Emerald,
    tertiary = TechniColors.GoldenTitle,
    background = TechniColors.Emerald,
    surface = TechniColors.Cream,
    onPrimary = TechniColors.Cream,
    onSecondary = TechniColors.Cream,
    onTertiary = TechniColors.Crimson,
    onBackground = TechniColors.Cream,
    onSurface = TechniColors.Crimson,
    primaryContainer = TechniColors.LessGolden,
    onPrimaryContainer = TechniColors.Crimson,
    error = TechniColors.Crimson,
    outline = TechniColors.GoldenTitle
)

private val TokyoNightColorScheme = darkColorScheme(
    primary = TechniColors.TokyoNightSurface,
    secondary = TechniColors.TokyoNightCyan,
    tertiary = TechniColors.TokyoNightMagenta,
    background = TechniColors.TokyoNightBg,
    surface = TechniColors.TokyoNightSurface,
    onPrimary = TechniColors.TokyoNightFg,
    onSecondary = TechniColors.TokyoNightBg,
    onTertiary = TechniColors.TokyoNightBg,
    onBackground = TechniColors.TokyoNightFg,
    onSurface = TechniColors.TokyoNightFg,
    primaryContainer = TechniColors.TokyoNightBg,
    onPrimaryContainer = TechniColors.TokyoNightCyan,
    error = TechniColors.TokyoNightRed,
    outline = TechniColors.TokyoNightBlue
)

enum class ThemeMode {
    LIGHT, DARK, TOKYO_NIGHT
}

@Composable
fun BananaProjectTheme(
    themeMode: ThemeMode = if (isSystemInDarkTheme()) ThemeMode.DARK else ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> TokyoNightColorScheme
        ThemeMode.TOKYO_NIGHT -> TokyoNightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
