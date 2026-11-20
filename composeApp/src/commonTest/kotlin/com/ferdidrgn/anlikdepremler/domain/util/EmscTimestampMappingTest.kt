package com.ferdidrgn.anlikdepremler.domain.util

import com.ferdidrgn.anlikdepremler.data.mapper.toDomain
import com.ferdidrgn.anlikdepremler.data.remote.dto.EmscFeature
import com.ferdidrgn.anlikdepremler.data.remote.dto.EmscGeometry
import com.ferdidrgn.anlikdepremler.data.remote.dto.EmscProperties
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmscTimestampMappingTest {
    @Test
    fun utcTimestampMapsToLocalDateAndTimeWithoutChangingItsInstant() {
        val rawTimestamp = "2026-11-19T00:15:42.987Z"
        val zone = TimeZone.currentSystemDefault()
        val expectedLocal = Instant.parse(rawTimestamp).toLocalDateTime(zone)

        val earthquake = EmscFeature(
            id = "emsc-test",
            geometry = EmscGeometry(coordinates = listOf(35.0, 39.0, 8.0)),
            properties = EmscProperties(time = rawTimestamp, mag = 4.2, depth = 8.0)
        ).toDomain()

        assertEquals(expectedLocal.date.toString(), earthquake.date)
        assertEquals(expectedLocal.time.toString().take(8), earthquake.time)
        assertEquals("EMSC", earthquake.source)

        val mappedInstant = assertNotNull(parseEarthquakeInstant(earthquake.date, earthquake.time, zone))
        assertTrue(kotlin.math.abs(mappedInstant.toEpochMilliseconds() - Instant.parse(rawTimestamp).toEpochMilliseconds()) < 1_000)
    }
}
