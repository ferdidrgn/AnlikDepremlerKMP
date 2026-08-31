package com.ferdidrgn.anlikdepremler

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ferdidrgn.anlikdepremler.app.App
import com.ferdidrgn.anlikdepremler.di.doInitKoin
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import kotlinx.browser.document
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    doInitKoin()
    val mainViewModel = KoinPlatform.getKoin().get<MainViewModel>()

    ComposeViewport(document.body!!) {
        App(mainViewModel = mainViewModel)
    }
}
