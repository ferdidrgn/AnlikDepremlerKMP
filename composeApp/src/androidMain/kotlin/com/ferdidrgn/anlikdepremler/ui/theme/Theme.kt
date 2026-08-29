package com.ferdidrgn.anlikdepremler.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode

// XML'deki Merienda Bold Fontunun Compose Tanımı
val MeriendaBold = FontFamily(
    Font(R.font.merienda_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MeriendaBold,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MeriendaBold,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MeriendaBold,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)
)

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

// 3. Senin Koyu Mavi Teman
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
    val context = LocalContext.current
    val colorScheme = when (themeMode) {
        AppThemeMode.CREAM_LIGHT -> CreamLightColors
        AppThemeMode.SYSTEM_DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                    context
                )
            } else {
                CreamLightColors
            }
        }

        AppThemeMode.DARK_NIGHT -> DarkNightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}