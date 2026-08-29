package com.ferdidrgn.anlikdepremler.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
import com.ferdidrgn.anlikdepremler.resources.Res
import org.jetbrains.compose.resources.Font

/** Android 12+ Material You dynamic color; null (and falls back to [CreamLightColors]) elsewhere. */
@Composable
expect fun dynamicColorSchemeOrNull(isDark: Boolean): ColorScheme?

@Composable
private fun appTypography(): Typography {
    val meriendaBold = FontFamily(Font(Res.font.merienda_bold, FontWeight.Bold))
    return Typography(
        displayLarge = TextStyle(
            fontFamily = meriendaBold,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = meriendaBold,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        titleLarge = TextStyle(
            fontFamily = meriendaBold,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)
    )
}

// 1. Krem Light Tema Renkleri
private val CreamLightColors = lightColorScheme(
    primary = Color(0xFFE2CD8A),
    onPrimary = Color(0xFF2D2D2D),
    primaryContainer = Color(0xFFF5EDE0),
    onPrimaryContainer = Color(0xFF3E3E3E),
    background = Color(0xFFEBE3D5),
    onBackground = Color(0xFF2D2D2D),
    surface = Color(0xFFF5F0E8),
    onSurface = Color(0xFF2D2D2D),
    surfaceVariant = Color(0xFFE8DCC8),
    onSurfaceVariant = Color(0xFF5A5A5A),
    error = Color(0xFFE85D5D)
)

// 3. Koyu Mavi Tema
private val DarkNightColors = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    background = Color(0xFF111827),
    onBackground = Color.White,
    surface = Color(0xFF1F2937),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color.LightGray,
    error = Color(0xFFEF4444)
)

@Composable
fun DepremTheme(
    themeMode: AppThemeMode = AppThemeMode.CREAM_LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.CREAM_LIGHT -> CreamLightColors
        AppThemeMode.SYSTEM_DYNAMIC ->
            dynamicColorSchemeOrNull(isDark = isSystemInDarkTheme()) ?: CreamLightColors

        AppThemeMode.DARK_NIGHT -> DarkNightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(),
        content = content
    )
}
