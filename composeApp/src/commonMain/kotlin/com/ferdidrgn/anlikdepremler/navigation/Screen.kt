package com.ferdidrgn.anlikdepremler.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ferdidrgn.anlikdepremler.resources.Res
import org.jetbrains.compose.resources.StringResource

sealed class Screen(val route: String, val titleRes: StringResource, val icon: ImageVector) {
    object Home : Screen("home", Res.string.nav_home, Icons.Default.Home)
    object Earthquakes : Screen("earthquakes", Res.string.nav_earthquakes, Icons.Default.List)
    object Map : Screen("map", Res.string.nav_map, Icons.Default.Map)
    object Settings : Screen("settings", Res.string.nav_settings, Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Earthquakes,
    Screen.Map,
    Screen.Settings
)
