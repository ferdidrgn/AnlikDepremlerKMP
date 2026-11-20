package com.ferdidrgn.anlikdepremler.domain.util

import com.ferdi.deprem.model.Earthquake
import kotlin.test.Test
import kotlin.test.assertEquals

class EarthquakeDeduplicatorTest {
    @Test
    fun removesOnlyCloseCrossSourceCatalogMatches() {
        val events = listOf(
            earthquake("afad-1", "AFAD", "12:00:00", 39.0, 35.0, 4.1),
            earthquake("usgs-1", "USGS", "12:01:00", 39.05, 35.02, 4.3),
            earthquake("afad-aftershock", "AFAD", "12:01:00", 39.05, 35.02, 4.3),
            earthquake("emsc-nearby", "EMSC", "12:00:30", 39.6, 35.4, 4.2)
        )

        assertEquals(
            listOf("afad-1", "afad-aftershock", "emsc-nearby"),
            events.deduplicateEarthquakes().map { it.id }
        )
    }

    @Test
    fun retainsDistinctEventsFromTheSameSourceAndDuplicateIdsOnlyOnce() {
        val events = listOf(
            earthquake("same-id", "AFAD", "12:00:00", 39.0, 35.0, 4.1),
            earthquake("same-id", "USGS", "12:00:00", 39.0, 35.0, 4.1),
            earthquake("same-source", "AFAD", "12:00:30", 39.0, 35.0, 4.1)
        )

        assertEquals(listOf("same-id", "same-source"), events.deduplicateEarthquakes().map { it.id })
    }

    private fun earthquake(id: String, source: String, time: String, lat: Double, lon: Double, magnitude: Double) =
        Earthquake(
            id = id,
            location = "Test",
            region = "Test region",
            magnitude = magnitude,
            depth = 8.0,
            date = "2026-11-20",
            time = time,
            latitude = lat,
            longitude = lon,
            cityImageUrl = "",
            source = source
        )
}
