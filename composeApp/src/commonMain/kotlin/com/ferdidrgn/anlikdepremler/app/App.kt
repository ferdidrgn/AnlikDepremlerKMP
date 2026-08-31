package com.ferdidrgn.anlikdepremler.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.theme.DepremTheme
import com.ferdidrgn.anlikdepremler.ui.web.WebDashboard

/** Shared entry point for the web (wasmJs) target: a dashboard layout, not a stretched port of the phone UI. */
@Composable
fun App(mainViewModel: MainViewModel) {
    val uiState by mainViewModel.uiState.collectAsState()

    DepremTheme(themeMode = uiState.currentTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            WebDashboard(mainViewModel = mainViewModel)
        }
    }
}
