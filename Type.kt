package org.banana.project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.banana.project.R

// 1. Define a FontFamily that includes your custom font
val BebasNeue = FontFamily(
    Font(R.font.bebas_neue, FontWeight.Normal)
)

// 2. Create a new Typography object, setting your custom font as the default.
//    This will apply it to all text styles that don't explicitly override the font family.
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    // You can customize other styles here as well, or just set a default.
    // For a simpler approach, you can set the default for all styles like this:
    bodyLarge = TextStyle(
        fontFamily = BebasNeue, // Set your font family here
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /*
    You can define other text styles like titleLarge, labelSmall, etc.
    If you don't specify a fontFamily, it will fall back to the default if you were to set one
    on the Typography constructor, or to the system default.
    A simpler Typography definition to apply the font everywhere could be:

    val AppTypography = Typography(
        defaultFontFamily = BebasNeue
    )
    This uses your font for all text styles, maintaining Material's size, weight, and spacing.
    */
)
