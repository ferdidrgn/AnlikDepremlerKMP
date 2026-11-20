package com.ferdidrgn.anlikdepremler

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.mutableStateOf
import com.ferdidrgn.anlikdepremler.app.App
import com.ferdidrgn.anlikdepremler.di.doInitKoin
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import kotlinx.browser.document
import kotlinx.browser.window
import org.koin.mp.KoinPlatform
import org.w3c.dom.HTMLElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    doInitKoin()
    val mainViewModel = KoinPlatform.getKoin().get<MainViewModel>()

    val appHost = document.getElementById("compose-app") as? HTMLElement
        ?: error("#compose-app container is missing from index.html")

    // The static HTML shell contains a useful loading state for slow networks. Remove it before
    // Compose mounts so it cannot remain in the accessibility tree or cover the generated canvas.
    appHost.innerHTML = ""

    fun syncPageVisibility() {
        mainViewModel.setAppForeground(isPageVisible())
    }
    document.addEventListener("visibilitychange", { syncPageVisibility() })
    syncPageVisibility()

    val currentEarthquakeId = mutableStateOf(earthquakeIdFromPath(window.location.pathname))
    window.addEventListener("popstate", {
        currentEarthquakeId.value = earthquakeIdFromPath(window.location.pathname)
    })

    ComposeViewport(appHost) {
        App(
            mainViewModel = mainViewModel,
            initialEarthquakeId = currentEarthquakeId.value,
            onEarthquakeUrlChanged = { id ->
                currentEarthquakeId.value = id
                updateEarthquakeUrl(id)
            },
            onShareEarthquake = ::shareCurrentEarthquakeUrl
        )
    }
}

private fun earthquakeIdFromPath(path: String): String? {
    val segments = path.trim('/').split('/').filter { it.isNotBlank() }
    return segments.takeIf { it.size >= 2 && it.first() == "earthquake" }
        ?.drop(1)
        ?.joinToString("/")
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun isPageVisible(): Boolean = js("document.visibilityState === 'visible'")

@OptIn(ExperimentalWasmJsInterop::class)
private fun updateEarthquakeUrl(id: String?): Unit = js(
    """{
        const nextPath = id ? '/earthquake/' + encodeURIComponent(id) : '/';
        if (window.location.pathname === nextPath) return;
        if (id) window.history.pushState(null, '', nextPath);
        else window.history.replaceState(null, '', nextPath);
    }"""
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun shareCurrentEarthquakeUrl(): Unit = js(
    """{
        const shareData = { title: document.title, url: window.location.href };
        if (navigator.share) {
            navigator.share(shareData).catch(() => {});
        } else if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(shareData.url)
                .then(() => window.alert('Bağlantı panoya kopyalandı.'))
                .catch(() => window.prompt('Deprem bağlantısını kopyalayın:', shareData.url));
        } else {
            window.prompt('Deprem bağlantısını kopyalayın:', shareData.url);
        }
    }"""
)
