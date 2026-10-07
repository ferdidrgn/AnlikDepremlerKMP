package com.ferdidrgn.anlikdepremler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.ferdidrgn.anlikdepremler.core.image.decodeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.compose.koinInject

/** Session-only in-memory cache, keyed by URL - avoids redecoding/reflashing the same image
 *  every time a card scrolls back on screen. Never persisted; nothing here needs it to be. */
private object RemoteImageCache {
    private val cache = mutableMapOf<String, ImageBitmap>()
    fun get(url: String): ImageBitmap? = cache[url]
    fun put(url: String, bitmap: ImageBitmap) {
        cache[url] = bitmap
    }
}

/**
 * Fetches and decodes a remote image (see [decodeImageBitmap] for why this is hand-rolled
 * instead of a Coil/image-loading library) and fades it in once ready. Shows [placeholder]
 * while loading or if the fetch/decode fails - callers should pass something that still looks
 * intentional on its own, not an empty box.
 */
@Composable
fun RemoteImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: @Composable () -> Unit = {}
) {
    val httpClient = koinInject<HttpClient>()
    var bitmap by remember(url) { mutableStateOf(RemoteImageCache.get(url)) }

    LaunchedEffect(url) {
        if (bitmap != null) return@LaunchedEffect
        val decoded = runCatching {
            val bytes = httpClient.get(url).body<ByteArray>()
            decodeImageBitmap(bytes)
        }.getOrNull()
        if (decoded != null) {
            RemoteImageCache.put(url, decoded)
            bitmap = decoded
        }
    }

    Box(modifier = modifier) {
        placeholder()
        val current = bitmap
        AnimatedVisibility(visible = current != null, enter = fadeIn()) {
            if (current != null) {
                Image(
                    bitmap = current,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
