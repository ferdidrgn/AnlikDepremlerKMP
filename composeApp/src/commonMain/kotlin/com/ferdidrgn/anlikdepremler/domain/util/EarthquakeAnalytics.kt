package com.ferdidrgn.anlikdepremler.domain.util

import com.ferdi.deprem.model.Earthquake
import com.ferdi.deprem.model.EarthquakeStatistics
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn

fun List<Earthquake>.calculateStatistics(): EarthquakeStatistics {
    return calculateStatistics(Clock.System.todayIn(TimeZone.currentSystemDefault()))
}

internal fun List<Earthquake>.calculateStatistics(today: LocalDate): EarthquakeStatistics {
    if (this.isEmpty()) {
        return EarthquakeStatistics(
            totalToday = 0,
            totalWeek = 0,
            totalMonth = 0,
            avgMagnitude = 0.0,
            maxMagnitude = 0.0,
            mostActiveRegion = "-",
            magnitudeDistribution = emptyMap()
        )
    }

    val sevenDaysAgo = today.minus(6, DateTimeUnit.DAY)
    val thirtyDaysAgo = today.minus(29, DateTimeUnit.DAY)

    val parsedDates = this.mapNotNull { parseEarthquakeDate(it.date) }
    val todayCount = parsedDates.count { it == today }
    val weekCount = parsedDates.count { it >= sevenDaysAgo && it <= today }
    val monthCount = parsedDates.count { it >= thirtyDaysAgo && it <= today }

    val avgMagnitude = this.map { it.magnitude }.average()
    val maxMagnitude = this.maxOfOrNull { it.magnitude } ?: 0.0

    val mostActiveRegion = this.groupBy { it.region }
        .maxByOrNull { it.value.size }?.key ?: "Bilinmiyor"

    val distribution = mapOf(
        "1-2" to this.count { it.magnitude >= 1.0 && it.magnitude < 2.0 },
        "2-3" to this.count { it.magnitude >= 2.0 && it.magnitude < 3.0 },
        "3-4" to this.count { it.magnitude >= 3.0 && it.magnitude < 4.0 },
        "4+" to this.count { it.magnitude >= 4.0 }
    )

    return EarthquakeStatistics(
        totalToday = todayCount,
        totalWeek = weekCount,
        totalMonth = monthCount,
        avgMagnitude = if (avgMagnitude.isNaN()) 0.0 else avgMagnitude,
        maxMagnitude = maxMagnitude,
        mostActiveRegion = mostActiveRegion,
        magnitudeDistribution = distribution
    )
}

fun List<Earthquake>.filterByTimeSpan(span: String): List<Earthquake> {
    return filterByTimeSpan(span, Clock.System.now(), TimeZone.currentSystemDefault())
}

internal fun List<Earthquake>.filterByTimeSpan(
    span: String,
    now: Instant,
    zone: TimeZone
): List<Earthquake> {
    val hours = when (span) {
        "1s" -> 1
        "6s" -> 6
        "24s" -> 24
        "7g" -> 24 * 7
        "30g" -> 24 * 30
        else -> return this
    }
    val threshold = now.minus(hours.toLong(), DateTimeUnit.HOUR)
    return filter { earthquake ->
        parseEarthquakeInstant(earthquake.date, earthquake.time, zone)
            ?.let { it >= threshold && it <= now }
            ?: false
    }
}

internal fun parseEarthquakeInstant(date: String, time: String, zone: TimeZone): Instant? =
    parseEarthquakeDateTime(date, time)?.toInstant(zone)

private fun parseEarthquakeDate(rawDate: String): LocalDate? {
    val value = rawDate.trim()
    if (value.isEmpty()) return null
    return runCatching {
        when {
            value.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> LocalDate.parse(value)
            value.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}")) -> {
                val (day, month, year) = value.split(".")
                LocalDate(year.toInt(), month.toInt(), day.toInt())
            }
            value.matches(Regex("\\d{2}-\\d{2}-\\d{4}")) -> {
                val (day, month, year) = value.split("-")
                LocalDate(year.toInt(), month.toInt(), day.toInt())
            }
            else -> LocalDate.parse(value.take(10))
        }
    }.getOrNull()
}

private fun parseEarthquakeDateTime(rawDate: String, rawTime: String): LocalDateTime? {
    val date = parseEarthquakeDate(rawDate) ?: return null
    val parts = rawTime.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val second = parts.getOrNull(2)?.take(2)?.toIntOrNull() ?: 0
    return runCatching {
        LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute, second)
    }.getOrNull()
}
