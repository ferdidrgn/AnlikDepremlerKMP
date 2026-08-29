package com.ferdidrgn.anlikdepremler.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS location lookup via CoreLocation. Requires NSLocationWhenInUseUsageDescription in
 * Info.plist and the user having granted location permission before calling this.
 * Reverse geocoding (city name) is not implemented yet — TODO: use CLGeocoder.
 */
@OptIn(ExperimentalForeignApi::class)
class IosLocationTracker : LocationTracker {

    private val manager = CLLocationManager()

    override suspend fun getCurrentLocation(): UserLocationResult? =
        suspendCancellableCoroutine { continuation ->
            manager.requestWhenInUseAuthorization()

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (location != null) {
                        val coordinate = location.coordinate
                        continuation.resume(
                            UserLocationResult(
                                latitude = coordinate.useContents { latitude },
                                longitude = coordinate.useContents { longitude },
                                cityName = ""
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    continuation.resume(null)
                }
            }

            manager.delegate = delegate
            manager.requestLocation()
        }
}
