package com.ferdidrgn.anlikdepremler.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ferdidrgn.anlikdepremler.core.ads.AdManager
import com.ferdidrgn.anlikdepremler.core.navigation.DeepLinkHelper
import com.ferdidrgn.anlikdepremler.ui.components.CustomBottomNavigationBar
import com.ferdidrgn.anlikdepremler.ui.components.OfflineBanner
import com.ferdidrgn.anlikdepremler.ui.screen.EarthquakeDetailScreen
import com.ferdidrgn.anlikdepremler.ui.screen.EarthquakeListScreen
import com.ferdidrgn.anlikdepremler.ui.screen.HistoricalArchiveScreen
import com.ferdidrgn.anlikdepremler.ui.screen.HomeScreen
import com.ferdidrgn.anlikdepremler.ui.screen.LegalDocumentScreen
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.screen.MapScreen
import com.ferdidrgn.anlikdepremler.ui.screen.SettingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel,
    adManager: AdManager
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()
    val isConnected by mainViewModel.isConnected.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            OfflineBanner(isConnected = isConnected)
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val shouldShowBottomBar = currentRoute != null &&
                    !currentRoute.startsWith("detail/") &&
                    !currentRoute.startsWith("legal/") &&
                    currentRoute != Screen.Map.route

            if (shouldShowBottomBar)
                CustomBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. ANA SAYFA
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = mainViewModel,
                    onEarthquakeClick = { selectedEq ->
                        val activity = context.findActivity()
                        if (activity != null) {
                            adManager.showInterstitial(activity) {
                                navController.navigate("detail/${selectedEq.id}")
                            }
                        } else {
                            navController.navigate("detail/${selectedEq.id}")
                        }
                    },
                    onSeeAllClick = {
                        navController.navigate(Screen.Earthquakes.route)
                    },
                    onHistoricalArchiveClick = {
                        navController.navigate("historical_archive")
                    }
                )
            }

            // 2. TÜM DEPREMLER LİSTESİ
            composable(Screen.Earthquakes.route) {
                EarthquakeListScreen(
                    viewModel = mainViewModel,
                    onEarthquakeClick = { selectedEq ->
                        val activity = context.findActivity()
                        if (activity != null)
                            adManager.showInterstitial(activity) {
                                navController.navigate("detail/${selectedEq.id}")
                            }
                        else
                            navController.navigate("detail/${selectedEq.id}")

                    }
                )
            }

            // 3. HARİTA EKRANI
            composable(Screen.Map.route) {
                MapScreen(
                    viewModel = mainViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. AYARLAR EKRANI
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = koinViewModel(),
                    onOpenLegalDocument = { docType ->
                        navController.navigate("legal/$docType")
                    }
                )
            }

            // 5. DEPREM DETAY EKRANI
            composable(
                route = "detail/{earthquakeId}",
                arguments = listOf(navArgument("earthquakeId") { type = NavType.StringType }),
                deepLinks = DeepLinkHelper.earthquakeDetailDeepLink
            ) { backStackEntry ->
                val eqId = backStackEntry.arguments?.getString("earthquakeId")
                val earthquake = uiState.earthquakes.find { it.id == eqId }

                if (earthquake != null)
                    EarthquakeDetailScreen(
                        earthquake = earthquake,
                        onBackClick = { navController.popBackStack() }
                    )

            }

            // 6. YASAL METİNLER EKRANI
            composable("legal/{docType}") { backStackEntry ->
                val docType = backStackEntry.arguments?.getString("docType") ?: "privacy_policy"
                LegalDocumentScreen(
                    documentType = docType,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("historical_archive") {
                HistoricalArchiveScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

// Activity bulucu Extension
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}