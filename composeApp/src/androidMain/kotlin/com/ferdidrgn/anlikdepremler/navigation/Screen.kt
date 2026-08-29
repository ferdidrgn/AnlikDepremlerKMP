package com.ferdidrgn.anlikdepremler.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ferdidrgn.anlikdepremler.R

sealed class Screen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Earthquakes : Screen("earthquakes", R.string.nav_earthquakes, Icons.Default.List)
    object Map : Screen("map", R.string.nav_map, Icons.Default.Map)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Earthquakes,
    Screen.Map,
    Screen.Settings
)