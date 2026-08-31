package com.ferdidrgn.anlikdepremler.core.util

/**
 * TODO: wire up the browser Geolocation API (navigator.geolocation.getCurrentPosition) via
 * kotlinx-browser. For now this always returns null so the shared UI logic isn't blocked.
 */
class WasmLocationTracker : LocationTracker {
    override suspend fun getCurrentLocation(): UserLocationResult? = null
}
