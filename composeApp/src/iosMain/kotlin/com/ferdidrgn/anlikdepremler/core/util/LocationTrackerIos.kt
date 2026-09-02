package com.ferdidrgn.anlikdepremler.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS location lookup via CoreLocation, followed by a CLGeocoder reverse-geocode of the fix to
 * fill in cityName. Requires NSLocationWhenInUseUsageDescription in Info.plist and the user
 * having granted location permission before calling this.
 */
@OptIn(ExperimentalForeignApi::class)
class IosLocationTracker : LocationTracker {

    private val manager = CLLocationManager()
    private val geocoder = CLGeocoder()

    override suspend fun getCurrentLocation(): UserLocationResult? =
        suspendCancellableCoroutine { continuation ->
            manager.requestWhenInUseAuthorization()

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (location == null) {
                        if (continuation.isActive) continuation.resume(null)
                        return
                    }

                    val coordinate = location.coordinate
                    val latitude = coordinate.useContents { latitude }
                    val longitude = coordinate.useContents { longitude }

                    geocoder.reverseGeocodeLocation(location) { placemarks, _ ->
                        // A failed/empty reverse-geocode still has a valid fix - fall back to
                        // an empty city name rather than dropping the whole location result.
                        val cityName = (placemarks?.firstOrNull() as? CLPlacemark)?.locality.orEmpty()
                        if (continuation.isActive) {
                            continuation.resume(
                                UserLocationResult(
                                    latitude = latitude,
                                    longitude = longitude,
                                    cityName = cityName
                                )
                            )
                        }
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }

            manager.delegate = delegate
            manager.requestLocation()
        }
}
