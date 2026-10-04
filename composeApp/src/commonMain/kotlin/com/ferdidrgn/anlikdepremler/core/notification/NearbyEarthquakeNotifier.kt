package com.ferdidrgn.anlikdepremler.core.notification

import com.ferdi.deprem.model.Earthquake

/**
 * Posts a local, on-device notification when MainViewModel's own proximity check (the user's
 * last known location against the already-fetched earthquake list) finds a nearby significant
 * earthquake. Separate from the server-driven FCM push (EarthquakeFirebaseMessagingService on
 * Android) - this one fires purely from data already on the device.
 */
interface NearbyEarthquakeNotifier {
    fun notifyNearbyEarthquake(earthquake: Earthquake, distanceKm: Double)
}

/** Used on platforms with no local-notification channel wired up yet (iOS, web). */
class NoOpNearbyEarthquakeNotifier : NearbyEarthquakeNotifier {
    override fun notifyNearbyEarthquake(earthquake: Earthquake, distanceKm: Double) = Unit
}
