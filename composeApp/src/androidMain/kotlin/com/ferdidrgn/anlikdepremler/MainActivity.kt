package com.ferdidrgn.anlikdepremler

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.ferdidrgn.anlikdepremler.core.ads.AdManager
import com.ferdidrgn.anlikdepremler.core.language.LocaleHelper
import com.ferdidrgn.anlikdepremler.core.util.CrashlyticsLogger
import com.ferdidrgn.anlikdepremler.navigation.AppNavigation
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.screen.OnboardingScreen
import com.ferdidrgn.anlikdepremler.ui.screen.SplashScreen
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode
import com.ferdidrgn.anlikdepremler.ui.theme.DepremTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : AppCompatActivity() {

    private val adManager: AdManager by inject()

    // 🎯 DİL DOKUNUŞU: UYGULAMA AÇILIRKEN KAYITLI DİLİ YÜKLEYEN KRİTİK METOT
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", "tr") ?: "tr"
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase, savedLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupUncaughtExceptionHandler()

        setContent {
            val mainViewModel: MainViewModel = koinViewModel()
            val uiState by mainViewModel.uiState.collectAsState()
            val isOnboardingCompleted by mainViewModel.isOnboardingCompleted.collectAsState()

            var isSplashActive by remember { mutableStateOf(true) }

            ConfigureSystemBars(themeMode = uiState.currentTheme)

            DepremTheme(themeMode = uiState.currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isSplashActive -> SplashScreen(onSplashFinished = {
                                isSplashActive = false
                            })


                            !isOnboardingCompleted -> OnboardingScreen(onFinishOnboarding = { mainViewModel.completeOnboarding() })


                            else ->
                                AppNavigation(
                                    mainViewModel = mainViewModel,
                                    adManager = adManager
                                )

                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ConfigureSystemBars(themeMode: AppThemeMode) {
        val isDarkTheme = when (themeMode) {
            AppThemeMode.DARK_NIGHT -> true
            AppThemeMode.CREAM_LIGHT -> false
            AppThemeMode.SYSTEM_DYNAMIC -> isSystemInDarkTheme()
        }

        SideEffect {
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    private fun setupUncaughtExceptionHandler() {
        try {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                CrashlyticsLogger.log("Uncaught Exception in Thread: ${thread.name}")
                CrashlyticsLogger.logException(throwable)
            }
        } catch (e: Exception) {
            CrashlyticsLogger.logException(e)
        }
    }
}