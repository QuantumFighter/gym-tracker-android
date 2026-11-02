package com.quantumfighter.gymtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quantumfighter.gymtracker.R

private val Barlow = FontFamily(Font(R.font.barlow_regular), Font(R.font.barlow_medium, FontWeight.Medium))
private fun type(size: Int, line: Int, medium: Boolean = false) = TextStyle(fontFamily = Barlow, fontWeight = if (medium) FontWeight.Medium else FontWeight.Normal, fontSize = size.sp, lineHeight = line.sp)

private val ApprovedTypography = Typography(
    displayLarge = type(64, 72, true), displayMedium = type(52, 60, true), displaySmall = type(44, 52, true),
    headlineLarge = type(34, 41, true), headlineMedium = type(30, 37, true), headlineSmall = type(26, 33, true),
    titleLarge = type(24, 30, true), titleMedium = type(20, 26, true), titleSmall = type(17, 23, true),
    bodyLarge = type(20, 28), bodyMedium = type(17, 24), bodySmall = type(14, 20),
    labelLarge = type(17, 22, true), labelMedium = type(14, 20, true), labelSmall = type(12, 16),
)

private val Light = lightColorScheme(
    primary = Color(0xFF3156F5), onPrimary = Color.White, primaryContainer = Color(0xFFDBE2F4), onPrimaryContainer = Color(0xFF3156F5),
    secondary = Color(0xFF646A80), onSecondary = Color.White, secondaryContainer = Color(0xFFDCE2EC), onSecondaryContainer = Color(0xFF202438),
    tertiary = Color(0xFF23834B), onTertiary = Color.White,
    background = Color(0xFFE9EDF3), onBackground = Color(0xFF202438),
    surface = Color(0xFFE9EDF3), onSurface = Color(0xFF202438), surfaceVariant = Color(0xFFDCE2EC), onSurfaceVariant = Color(0xFF646A80),
    surfaceContainer = Color(0xFFDCE2EC), surfaceContainerLow = Color(0xFFF2F4F8), surfaceContainerHigh = Color(0xFFDCE2EC), surfaceContainerHighest = Color(0xFFDCE2EC),
    outline = Color(0xFF646A80), outlineVariant = Color(0xFFD5DCE6),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF718AFF), onPrimary = Color(0xFF111B43), primaryContainer = Color(0xFF2A3552), onPrimaryContainer = Color(0xFF91A3FF),
    secondary = Color(0xFFAEB5CE), onSecondary = Color(0xFF1D222C), secondaryContainer = Color(0xFF343E4B), onSecondaryContainer = Color(0xFFF4F5FF),
    tertiary = Color(0xFF79D79B), onTertiary = Color(0xFF07391B),
    background = Color(0xFF1D222C), onBackground = Color(0xFFF4F5FF),
    surface = Color(0xFF1D222C), onSurface = Color(0xFFF4F5FF), surfaceVariant = Color(0xFF252D3B), onSurfaceVariant = Color(0xFFAEB5CE),
    surfaceContainer = Color(0xFF252D3B), surfaceContainerLow = Color(0xFF252D3B), surfaceContainerHigh = Color(0xFF343E4B), surfaceContainerHighest = Color(0xFF303A4B),
    outline = Color(0xFFAEB5CE), outlineVariant = Color(0xFF343E4B),
)

/**
 * Supplies Material 3 colours that follow the system's light or dark appearance.
 *
 * @param content UI rendered inside the app's Material theme.
 */
@Composable
fun GymTrackerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = ApprovedTypography,
        content = content,
    )
}
