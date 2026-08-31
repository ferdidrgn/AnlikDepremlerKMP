package com.ferdidrgn.anlikdepremler.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ferdidrgn.anlikdepremler.resources.Res
import com.ferdidrgn.anlikdepremler.resources.app_name
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode
import com.ferdidrgn.anlikdepremler.ui.theme.DepremTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Shared entry point currently used by the web (wasmJs) target only. Android keeps its own
 * full navigation graph in MainActivity/AppNavigation (androidMain) - the real screens
 * (HomeScreen, MapScreen, etc.) haven't been ported to commonMain yet, so this is a
 * placeholder proving the shared theme/resources pipeline renders on web.
 */
@Composable
fun App() {
    DepremTheme(themeMode = AppThemeMode.CREAM_LIGHT) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}
