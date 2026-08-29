package com.ferdidrgn.anlikdepremler.core.util

data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = ""
)

/** Platform-specific device location lookup (Android: FusedLocationProvider, iOS: CoreLocation). */
interface LocationTracker {
    suspend fun getCurrentLocation(): UserLocationResult?
}
