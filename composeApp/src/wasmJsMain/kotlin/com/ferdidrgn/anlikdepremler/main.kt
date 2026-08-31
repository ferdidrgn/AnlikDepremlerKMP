package com.ferdidrgn.anlikdepremler

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ferdidrgn.anlikdepremler.app.App
import kotlinx.browser.document

// TODO: once the shared App() actually needs injected dependencies (repository, view models),
// call di.doInitKoin() here first - deliberately skipped for now since the wasmJs
// DataStore/PreferenceDataStoreFactory path (see di/PlatformModule.kt) is unverified in a real
// browser and shouldn't be able to crash this minimal render-only entry point.
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
