package com.ferdidrgn.anlikdepremler.core.util

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    private fun Double.toRadians(): Double = this * PI / 180.0

    /**
     * İki koordinat arasındaki mesafeyi kilometre (KM) cinsinden hesaplar (haversine formülü).
     */
    fun calculateDistanceInKm(
        userLat: Double,
        userLng: Double,
        eqLat: Double,
        eqLng: Double
    ): Double {
        val dLat = (eqLat - userLat).toRadians()
        val dLng = (eqLng - userLng).toRadians()
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(userLat.toRadians()) * cos(eqLat.toRadians()) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}
