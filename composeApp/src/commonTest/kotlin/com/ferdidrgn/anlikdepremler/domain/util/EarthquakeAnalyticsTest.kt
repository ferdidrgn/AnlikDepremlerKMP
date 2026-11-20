package com.ferdidrgn.anlikdepremler.domain.util

import com.ferdi.deprem.model.Earthquake
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class EarthquakeAnalyticsTest {

    private val now = Instant.parse("2026-11-18T12:00:00Z")
    private val utc = TimeZone.UTC

    @Test
    fun oneHourFilterSupportsSourceDateFormatsAndBoundary() {
        val earthquakes = listOf(
            earthquake("iso", "2026-11-18", "11:30:00"),
            earthquake("dot", "18.11.2026", "11:00:00"),
            earthquake("dash", "18-11-2026", "10:59:59"),
            earthquake("invalid", "unknown", "11:45:00")
        )

        val result = earthquakes.filterByTimeSpan("1s", now, utc)

        assertEquals(listOf("iso", "dot"), result.map { it.id })
    }

    @Test
    fun filterExcludesFutureEventsAndKeepsUnknownSpanUnchanged() {
        val earthquakes = listOf(
            earthquake("current", "2026-11-18", "12:00:00"),
            earthquake("future", "2026-11-18", "12:00:01")
        )

        assertEquals(listOf("current"), earthquakes.filterByTimeSpan("24s", now, utc).map { it.id })
        assertEquals(earthquakes, earthquakes.filterByTimeSpan("all", now, utc))
    }

    @Test
    fun calendarStatisticsUseSevenAndThirtyInclusiveDaysAndExcludeFuture() {
        val earthquakes = listOf(
            earthquake("today", "2026-11-18", "08:00:00"),
            earthquake("week-boundary", "2026-11-12", "08:00:00"),
            earthquake("week-outside", "2026-11-11", "08:00:00"),
            earthquake("month-boundary", "2026-10-20", "08:00:00"),
            earthquake("month-outside", "2026-10-19", "08:00:00"),
            earthquake("future", "2026-11-19", "08:00:00")
        )

        val statistics = earthquakes.calculateStatistics(LocalDate(2026, 11, 18))

        assertEquals(1, statistics.totalToday)
        assertEquals(2, statistics.totalWeek)
        assertEquals(4, statistics.totalMonth)
    }

    private fun earthquake(id: String, date: String, time: String) = Earthquake(
        id = id,
        location = "Test",
        magnitude = 3.0,
        depth = 5.0,
        date = date,
        time = time,
        latitude = 39.0,
        longitude = 35.0,
        cityImageUrl = ""
    )
}
