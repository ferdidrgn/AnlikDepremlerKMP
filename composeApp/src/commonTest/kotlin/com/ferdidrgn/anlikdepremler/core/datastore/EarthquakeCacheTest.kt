package com.ferdidrgn.anlikdepremler.core.datastore

import com.ferdi.deprem.model.Earthquake
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class EarthquakeCacheTest {
    @Test
    fun cachedSnapshotRoundTripsAllEarthquakeFields() {
        val expected = Earthquake(
            id = "event-42",
            location = "Aegean Sea",
            region = "Izmir",
            magnitude = 4.2,
            depth = 8.5,
            date = "2026-11-20",
            time = "10:15:30",
            latitude = 38.42,
            longitude = 26.91,
            cityImageUrl = "",
            isSignificant = false,
            intensity = "-",
            source = "AFAD"
        )
        val snapshot = StoredEarthquakeSnapshot(
            cachedAtEpochMillis = 1_795_174_530_000,
            sourceName = "AFAD",
            earthquakes = listOf(StoredEarthquake.from(expected))
        )

        val decoded = Json.decodeFromString<StoredEarthquakeSnapshot>(Json.encodeToString(snapshot))

        assertEquals(snapshot.cachedAtEpochMillis, decoded.cachedAtEpochMillis)
        assertEquals(snapshot.sourceName, decoded.sourceName)
        assertEquals(expected, decoded.earthquakes.single().toEarthquake())
    }
}
