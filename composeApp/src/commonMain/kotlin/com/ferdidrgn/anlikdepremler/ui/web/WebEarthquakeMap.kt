package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor

/** Turkey's rough bounding box - the default "frame" the map plots against when every point
 *  already falls inside it (the Kandilli/AFAD/Türkiye Karışık sources). World sources (USGS,
 *  EMSC, IGP) auto-widen it below instead of clipping points off the edge. */
private const val TURKEY_MIN_LAT = 34.0
private const val TURKEY_MAX_LAT = 43.0
private const val TURKEY_MIN_LON = 25.0
private const val TURKEY_MAX_LON = 45.0

private data class MapBounds(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)

/**
 * A lightweight coordinate-plot "map" - not a tiled slippy map (no basemap imagery/street data,
 * which would mean embedding a JS mapping library we haven't vetted against this project's
 * wasmJs toolchain yet), but a real, live, animated spatial read of where the current list's
 * earthquakes are and how strong each one is, which a plain table/list doesn't convey at all.
 */
@Composable
fun EarthquakeMapCanvas(earthquakes: List<Earthquake>, modifier: Modifier = Modifier) {
    val bounds = remember(earthquakes) { computeBounds(earthquakes) }
    val strongest = remember(earthquakes) { earthquakes.maxByOrNull { it.magnitude } }

    val transition = rememberInfiniteTransition(label = "mapPulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "mapPulseScale"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "mapPulseAlpha"
    )

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        if (earthquakes.isEmpty()) {
            Text(
                "Haritada gösterilecek deprem yok.",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            return@Box
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            // Graticule - a handful of evenly-spaced latitude/longitude guide lines so the plot
            // reads as a map, not an abstract scatter chart.
            val gridLines = 5
            for (i in 1 until gridLines) {
                val x = size.width * i / gridLines
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                val y = size.height * i / gridLines
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            drawRect(surfaceVariant.copy(alpha = 0.08f))

            earthquakes.forEach { eq ->
                val point = project(eq.latitude, eq.longitude, bounds, size.width, size.height)
                val color = magnitudeHeatColor(eq.magnitude)
                val radius = (4f + (eq.magnitude.toFloat().coerceIn(0f, 8f) * 2.2f))

                if (eq === strongest) {
                    drawCircle(
                        color = color.copy(alpha = pulseAlpha),
                        radius = radius * pulseScale,
                        center = point
                    )
                }
                drawCircle(color = color.copy(alpha = 0.85f), radius = radius, center = point)
                drawCircle(
                    color = color,
                    radius = radius,
                    center = point,
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

private fun computeBounds(earthquakes: List<Earthquake>): MapBounds {
    if (earthquakes.isEmpty()) {
        return MapBounds(TURKEY_MIN_LAT, TURKEY_MAX_LAT, TURKEY_MIN_LON, TURKEY_MAX_LON)
    }
    val lats = earthquakes.map { it.latitude }
    val lons = earthquakes.map { it.longitude }
    // Pad by ~8% of the point spread so edge points aren't drawn flush against the border.
    val latSpan = ((lats.maxOrNull() ?: TURKEY_MAX_LAT) - (lats.minOrNull() ?: TURKEY_MIN_LAT)).coerceAtLeast(1.0)
    val lonSpan = ((lons.maxOrNull() ?: TURKEY_MAX_LON) - (lons.minOrNull() ?: TURKEY_MIN_LON)).coerceAtLeast(1.0)
    val latPad = latSpan * 0.08
    val lonPad = lonSpan * 0.08

    return MapBounds(
        minLat = minOf(TURKEY_MIN_LAT, (lats.minOrNull() ?: TURKEY_MIN_LAT) - latPad),
        maxLat = maxOf(TURKEY_MAX_LAT, (lats.maxOrNull() ?: TURKEY_MAX_LAT) + latPad),
        minLon = minOf(TURKEY_MIN_LON, (lons.minOrNull() ?: TURKEY_MIN_LON) - lonPad),
        maxLon = maxOf(TURKEY_MAX_LON, (lons.maxOrNull() ?: TURKEY_MAX_LON) + lonPad)
    )
}

private fun project(lat: Double, lon: Double, bounds: MapBounds, width: Float, height: Float): Offset {
    val latSpan = (bounds.maxLat - bounds.minLat).takeIf { it > 0.0 } ?: 1.0
    val lonSpan = (bounds.maxLon - bounds.minLon).takeIf { it > 0.0 } ?: 1.0
    val x = ((lon - bounds.minLon) / lonSpan) * width
    // Latitude increases upward in the real world but screen-space y increases downward.
    val y = height - ((lat - bounds.minLat) / latSpan) * height
    return Offset(x.toFloat(), y.toFloat())
}
