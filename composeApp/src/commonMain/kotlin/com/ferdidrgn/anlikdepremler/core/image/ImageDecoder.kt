package com.ferdidrgn.anlikdepremler.core.image

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes raw encoded image bytes (JPEG/PNG/WebP) into a Compose [ImageBitmap]. Not using a
 * multiplatform image-loading library here (e.g. Coil 3) deliberately - this project's wasmJs
 * toolchain took a long time to stabilize this session (Kotlin/Compose version bumps, Gradle
 * repository resolution, Node/Yarn/Binaryen plugin churn), so a small, self-contained decoder
 * with no new transitive dependencies is the lower-risk way to get real remote images on web.
 * Android decodes via the platform's own BitmapFactory; iOS and wasmJs both render through Skia
 * already (Compose Multiplatform's non-Android backend), so they share the same Skia-based decode.
 */
expect suspend fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
