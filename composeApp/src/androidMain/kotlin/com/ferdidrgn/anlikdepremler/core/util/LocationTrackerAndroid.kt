package com.ferdidrgn.anlikdepremler.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class AndroidLocationTracker(private val context: Context) : LocationTracker {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocationResult? =
        suspendCancellableCoroutine { continuation ->
            client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val cityName = getCityNameFromCoordinates(location.latitude, location.longitude)
                    continuation.resume(
                        UserLocationResult(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            cityName = cityName
                        )
                    )
                } else {
                    continuation.resume(null)
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }
        }

    private fun getCityNameFromCoordinates(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var name = ""
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        name = addresses[0].adminArea ?: addresses[0].subAdminArea ?: ""
                    }
                }
                name
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].adminArea ?: addresses[0].subAdminArea ?: ""
                } else ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
