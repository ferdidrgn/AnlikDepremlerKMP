package com.ferdidrgn.anlikdepremler.domain.util

import com.ferdi.deprem.model.Earthquake
import kotlinx.datetime.TimeZone
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.abs

/**
 * Removes only high-confidence cross-catalog matches. Time, distance, and magnitude all need
 * to agree; nearby aftershocks remain separate when their origin times or magnitudes differ.
 * The source catalog's first record is retained because this lightweight model has no field for
 * alternate-catalog provenance.
 */
fun List<Earthquake>.deduplicateEarthquakes(
    timeToleranceMillis: Long = 2 * 60 * 1000L,
    distanceToleranceKm: Double = 15.0,
    magnitudeTolerance: Double = 0.8
): List<Earthquake> {
    val unique = mutableListOf<Earthquake>()
    val seenIds = mutableSetOf<String>()
    val timeZone = TimeZone.currentSystemDefault()

    for (candidate in this) {
        if (!seenIds.add(candidate.id)) continue
        val candidateTime = parseEarthquakeInstant(candidate.date, candidate.time, timeZone)
            ?.toEpochMilliseconds()
        val duplicate = candidateTime != null && unique.any { existing ->
            if (existing.source.equals(candidate.source, ignoreCase = true)) return@any false
            val existingTime = parseEarthquakeInstant(existing.date, existing.time, timeZone)
                ?.toEpochMilliseconds() ?: return@any false
            val timeMatches = abs(existingTime - candidateTime) <= timeToleranceMillis
            val magnitudeMatches = abs(existing.magnitude - candidate.magnitude) <= magnitudeTolerance
            timeMatches && magnitudeMatches && distanceKm(existing, candidate) <= distanceToleranceKm
        }
        if (!duplicate) unique += candidate
    }

    return unique
}

private fun distanceKm(first: Earthquake, second: Earthquake): Double {
    if (first.latitude !in -90.0..90.0 || second.latitude !in -90.0..90.0 ||
        first.longitude !in -180.0..180.0 || second.longitude !in -180.0..180.0
    ) return Double.POSITIVE_INFINITY

    val earthRadiusKm = 6371.0
    val lat1 = first.latitude * kotlin.math.PI / 180.0
    val lat2 = second.latitude * kotlin.math.PI / 180.0
    val deltaLat = (second.latitude - first.latitude) * kotlin.math.PI / 180.0
    val deltaLon = (second.longitude - first.longitude) * kotlin.math.PI / 180.0
    val haversine = sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
}
