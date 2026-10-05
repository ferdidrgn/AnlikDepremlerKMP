package com.ferdidrgn.anlikdepremler.core.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Forward-geocodes a free-text place name (e.g. "Kadıköy, İstanbul") to coordinates, for the
 * "saved locations" nearby-earthquake feature - mirrors AndroidLocationTracker's reverse
 * geocoding pattern (coordinates -> name) in the other direction.
 */
suspend fun geocodeLocationName(context: Context, query: String): Pair<Double, Double>? =
    suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(query, 1) { addresses ->
                    val result = addresses.firstOrNull()?.let { it.latitude to it.longitude }
                    if (continuation.isActive) continuation.resume(result)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                val result = addresses?.firstOrNull()?.let { it.latitude to it.longitude }
                continuation.resume(result)
            }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(null)
        }
    }
