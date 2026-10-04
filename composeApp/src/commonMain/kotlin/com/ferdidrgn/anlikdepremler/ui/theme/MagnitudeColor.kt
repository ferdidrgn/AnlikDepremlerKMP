package com.ferdidrgn.anlikdepremler.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * USGS-ShakeMap-style continuous color gradient across the earthquake magnitude scale, shared
 * by the Android and web UIs so a given magnitude always reads the same color everywhere -
 * teal (negligible) -> amber (minor) -> orange (moderate) -> crimson (severe).
 */
private val MagnitudeColorStops = listOf(
    0.0 to Color(0xFF14B8A6),
    2.5 to Color(0xFFF2B300),
    4.0 to Color(0xFFF2780C),
    6.0 to Color(0xFFDC2626)
)

fun magnitudeHeatColor(magnitude: Double): Color {
    val stops = MagnitudeColorStops
    if (magnitude <= stops.first().first) return stops.first().second
    if (magnitude >= stops.last().first) return stops.last().second

    for (i in 0 until stops.size - 1) {
        val (lowValue, lowColor) = stops[i]
        val (highValue, highColor) = stops[i + 1]
        if (magnitude in lowValue..highValue) {
            val fraction = ((magnitude - lowValue) / (highValue - lowValue)).toFloat()
            return lerp(lowColor, highColor, fraction)
        }
    }
    return stops.last().second
}

/** Maps a magnitudeDistribution bucket key ("1-2", "4+", ...) to a representative magnitude. */
fun magnitudeRangeMidpoint(range: String): Double {
    val lowerBound = range.trimEnd('+').substringBefore('-').toDoubleOrNull() ?: 0.0
    return if (range.endsWith('+')) lowerBound + 1.0 else lowerBound + 0.5
}
