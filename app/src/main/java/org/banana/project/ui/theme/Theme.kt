package org.banana.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3)
)

private val LightColorScheme = lightColorScheme(
    primary = TechniColors.Crimson,
    secondary = TechniColors.Emerald,
    tertiary = TechniColors.Goldenrod,
    background = TechniColors.Cream,
    surface = TechniColors.Cream,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = TechniColors.Crimson,
    outline = TechniColors.Emerald
)

private val TokyoNightColorScheme = darkColorScheme(
    primary = TechniColors.TokyoNightBlue,
    secondary = TechniColors.TokyoNightCyan,
    tertiary = TechniColors.TokyoNightMagenta,
    background = TechniColors.TokyoNightBg,
    surface = TechniColors.TokyoNightSurface,
    onPrimary = TechniColors.TokyoNightBg,
    onSecondary = TechniColors.TokyoNightBg,
    onTertiary = TechniColors.TokyoNightBg,
    onBackground = TechniColors.TokyoNightFg,
    onSurface = TechniColors.TokyoNightFg,
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
