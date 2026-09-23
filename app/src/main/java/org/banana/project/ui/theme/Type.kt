package org.banana.project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.banana.project.R

// Outfit is the same font family used across the web app.
// Static weights are bundled under res/font to match the web's Tailwind weights.
val Outfit = FontFamily(
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
    Font(R.font.outfit_black, FontWeight.Black)
)

// Apply Outfit to every Material3 text style so all components
// (buttons, labels, titles, text fields, etc.) use the web font family.
private val BaseTypography = Typography()

val AppTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = Outfit),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = Outfit),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = Outfit),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = Outfit),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = Outfit),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = Outfit),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = Outfit),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = Outfit),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = Outfit),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = Outfit),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = Outfit),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = Outfit),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = Outfit),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = Outfit),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = Outfit)
)
