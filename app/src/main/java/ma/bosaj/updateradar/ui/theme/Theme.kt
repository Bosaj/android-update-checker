package ma.bosaj.updateradar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Status colours used everywhere: fresh / aging / stale. */
@Immutable
data class RadarColors(val fresh: Color, val aging: Color, val stale: Color, val grid: Color, val sweep: Color)

val LocalRadar = staticCompositionLocalOf {
    RadarColors(Color(0xFF2FE0BE), Color(0xFFF5B841), Color(0xFFFF6B6B), Color(0x3348E5C8), Color(0xFF2FE0BE))
}

private val Dark = darkColorScheme(
    primary = Color(0xFF3BE3C4),
    onPrimary = Color(0xFF00201A),
    primaryContainer = Color(0xFF0D3B35),
    onPrimaryContainer = Color(0xFFB6FFEF),
    secondary = Color(0xFFF5B841),
    onSecondary = Color(0xFF2A1B00),
    secondaryContainer = Color(0xFF16332E),
    onSecondaryContainer = Color(0xFFB6FFEF),
    tertiary = Color(0xFF8AA7FF),
    background = Color(0xFF060A12),
    onBackground = Color(0xFFE7EDF6),
    surface = Color(0xFF0B111D),
    onSurface = Color(0xFFE7EDF6),
    surfaceVariant = Color(0xFF141C2B),
    onSurfaceVariant = Color(0xFF9AA7BC),
    surfaceContainer = Color(0xFF101828),
    surfaceContainerHigh = Color(0xFF162033),
    surfaceContainerHighest = Color(0xFF1C273D),
    outline = Color(0xFF2A3650),
    outlineVariant = Color(0xFF1E2840),
    error = Color(0xFFFF6B6B),
)

private val Light = lightColorScheme(
    primary = Color(0xFF00806C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F5E8),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF9A6A00),
    secondaryContainer = Color(0xFFCDEFE7),
    onSecondaryContainer = Color(0xFF00201A),
    tertiary = Color(0xFF3F5BB5),
    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF0E1522),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0E1522),
    surfaceVariant = Color(0xFFE7ECF4),
    onSurfaceVariant = Color(0xFF4F5B70),
    surfaceContainer = Color(0xFFEEF2F8),
    surfaceContainerHigh = Color(0xFFE6EBF3),
    surfaceContainerHighest = Color(0xFFDDE3EE),
    outline = Color(0xFFC3CCDA),
    outlineVariant = Color(0xFFDCE2EC),
    error = Color(0xFFD1373A),
)

private val Type = Typography(
    displaySmall = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun UpdateRadarTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val radar = if (dark) RadarColors(Color(0xFF2FE0BE), Color(0xFFF5B841), Color(0xFFFF6B6B), Color(0x2E48E5C8), Color(0xFF2FE0BE))
    else RadarColors(Color(0xFF00A383), Color(0xFFC98A00), Color(0xFFD9424A), Color(0x2600806C), Color(0xFF00A383))
    androidx.compose.runtime.CompositionLocalProvider(LocalRadar provides radar) {
        MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, content = content)
    }
}
