package com.ferdidrgn.anlikdepremler.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** No Material You dynamic color on iOS — callers fall back to [AppThemeMode.CREAM_LIGHT] colors. */
@Composable
actual fun dynamicColorSchemeOrNull(isDark: Boolean): ColorScheme? = null
