package com.ferdidrgn.anlikdepremler.core.util

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private fun requestBrowserLocation(
    onSuccess: (Double, Double) -> Unit,
    onError: () -> Unit
): Unit = js(
    """{
        if (!navigator.geolocation) {
            onError();
            return;
        }
        navigator.geolocation.getCurrentPosition(
            function(position) { onSuccess(position.coords.latitude, position.coords.longitude); },
            function(error) { onError(); }
        );
    }"""
)

/**
 * Browser Geolocation API. Requires the user to grant the browser's own location permission
 * prompt; reverse geocoding (city name) isn't implemented here, only the lat/lng fix.
 */
class WasmLocationTracker : LocationTracker {
    override suspend fun getCurrentLocation(): UserLocationResult? =
        suspendCancellableCoroutine { continuation ->
            requestBrowserLocation(
                onSuccess = { latitude, longitude ->
                    if (continuation.isActive) {
                        continuation.resume(
                            UserLocationResult(latitude = latitude, longitude = longitude, cityName = "")
                        )
                    }
                },
                onError = {
                    if (continuation.isActive) continuation.resume(null)
                }
            )
        }
}
