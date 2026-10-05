package com.ferdidrgn.anlikdepremler.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ferdidrgn.anlikdepremler.core.ads.AdManager
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.core.navigation.DeepLinkHelper
import com.ferdidrgn.anlikdepremler.core.sensor.ShakeDetector
import com.ferdidrgn.anlikdepremler.ui.components.CustomBottomNavigationBar
import com.ferdidrgn.anlikdepremler.ui.components.DropCoverHoldOverlay
import com.ferdidrgn.anlikdepremler.ui.components.OfflineBanner
import com.ferdidrgn.anlikdepremler.ui.screen.EarthquakeDetailScreen
import com.ferdidrgn.anlikdepremler.ui.screen.EarthquakeJournalScreen
import com.ferdidrgn.anlikdepremler.ui.screen.EarthquakeListScreen
import com.ferdidrgn.anlikdepremler.ui.screen.HistoricalArchiveScreen
import com.ferdidrgn.anlikdepremler.ui.screen.HomeScreen
import com.ferdidrgn.anlikdepremler.ui.screen.LegalDocumentScreen
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.screen.MapScreen
import com.ferdidrgn.anlikdepremler.ui.screen.SettingsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel,
    adManager: AdManager
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()
    val isConnected by mainViewModel.isConnected.collectAsState()
    val context = LocalContext.current

    // Pauses MainViewModel's auto-refresh while the app isn't visible - no point polling the
    // free third-party earthquake APIs on a timer while the user isn't even looking at the app.
    val currentViewModel by rememberUpdatedState(mainViewModel)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentViewModel.setAppForeground(true)
                Lifecycle.Event.ON_STOP -> currentViewModel.setAppForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Deneysel, cihaz üstü (backend'siz) sarsıntı algılama - sadece uygulama önplandayken
    // çalışır, kullanıcı Ayarlar'dan açtıysa. MK Earthquake Monitor'daki tek-telefon eşik
    // yöntemiyle aynı mantık; yanlış alarm verebilir, bu yüzden varsayılan kapalı.
    val preferencesManager: PreferencesManager = koinInject()
    val adsFreeUntilMillis by preferencesManager.adsFreeUntilMillis.collectAsState(initial = 0L)
    val isAdsFree = adsFreeUntilMillis > System.currentTimeMillis()
    val shakeDetectionEnabled by preferencesManager.shakeDetectionEnabled.collectAsState(initial = false)
    val shakeSensitivity by preferencesManager.shakeSensitivity.collectAsState(initial = ShakeDetector.SENSITIVITY_MEDIUM)
    var shakeTriggered by remember { mutableStateOf(false) }

    DisposableEffect(shakeDetectionEnabled, shakeSensitivity) {
        if (!shakeDetectionEnabled) return@DisposableEffect onDispose {}

        val detector = ShakeDetector(context, shakeSensitivity)
        detector.start { shakeTriggered = true }
        onDispose { detector.stop() }
    }

    if (shakeTriggered) {
        DropCoverHoldOverlay(onDismiss = { shakeTriggered = false })
    }

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
                        if (activity != null && !isAdsFree) {
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
                        if (activity != null && !isAdsFree)
                            adManager.showInterstitial(activity) {
                                navController.navigate("detail/${selectedEq.id}")
                            }
                        else
                            navController.navigate("detail/${selectedEq.id}")

                    }
                )
            }

            // 3. HARİTA EKRANI
            composable(
                route = Screen.Map.route,
                deepLinks = DeepLinkHelper.mapDeepLink
            ) {
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
                    },
                    onOpenJournal = {
                        navController.navigate("earthquake_journal")
                    }
                )
            }

            composable("earthquake_journal") {
                EarthquakeJournalScreen(onBackClick = { navController.popBackStack() })
            }

            // 5. DEPREM DETAY EKRANI
            composable(
                route = "detail/{earthquakeId}",
                arguments = listOf(navArgument("earthquakeId") { type = NavType.StringType }),
                deepLinks = DeepLinkHelper.earthquakeDetailDeepLink
            ) { backStackEntry ->
                val eqId = backStackEntry.arguments?.getString("earthquakeId")
                // rawEarthquakes (not the time-filtered `earthquakes`) so a deep link still
                // resolves an earthquake that's outside the currently selected time window.
                val earthquake = uiState.rawEarthquakes.find { it.id == eqId }

                when {
                    earthquake != null -> EarthquakeDetailScreen(
                        earthquake = earthquake,
                        onBackClick = { navController.popBackStack() }
                    )

                    uiState.isLoading -> DeepLinkLoadingState()

                    else -> DeepLinkNotFoundState(onBackClick = { navController.popBackStack() })
                }
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

@Composable
private fun DeepLinkLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = stringResource(com.ferdidrgn.anlikdepremler.R.string.deeplink_loading),
            modifier = Modifier.padding(top = 16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeepLinkNotFoundState(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = stringResource(com.ferdidrgn.anlikdepremler.R.string.deeplink_not_found_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(com.ferdidrgn.anlikdepremler.R.string.deeplink_not_found_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        Button(onClick = onBackClick) {
            Text(stringResource(com.ferdidrgn.anlikdepremler.R.string.back))
        }
    }
}