package com.ferdidrgn.anlikdepremler.domain.util

import com.ferdi.deprem.model.Earthquake
import com.ferdi.deprem.model.EarthquakeStatistics
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

fun List<Earthquake>.calculateStatistics(): EarthquakeStatistics {
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

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val sevenDaysAgo = today.minus(7, DateTimeUnit.DAY)
    val thirtyDaysAgo = today.minus(30, DateTimeUnit.DAY)

    var weekCount = 0
    var monthCount = 0

    this.forEach { eq ->
        try {
            val parsedDate = LocalDate.parse(eq.date.replace(".", "-"))
            if (parsedDate >= sevenDaysAgo) weekCount++
            if (parsedDate >= thirtyDaysAgo) monthCount++
        } catch (e: Exception) {
            weekCount++
            monthCount++
        }
    }

    val avgMagnitude = this.map { it.magnitude }.average()
    val maxMagnitude = this.maxOfOrNull { it.magnitude } ?: 0.0

    val mostActiveRegion = this.groupBy { it.region }
        .maxByOrNull { it.value.size }?.key ?: "Bilinmiyor"

    val distribution = mapOf(
        "1-2" to this.count { it.magnitude in 1.0..2.0 },
        "2-3" to this.count { it.magnitude in 2.0..3.0 },
        "3-4" to this.count { it.magnitude in 3.0..4.0 },
        "4+" to this.count { it.magnitude >= 4.0 }
    )

    return EarthquakeStatistics(
        totalToday = this.size,
        totalWeek = if (weekCount > 0) weekCount else this.size,
        totalMonth = if (monthCount > 0) monthCount else this.size,
        avgMagnitude = if (avgMagnitude.isNaN()) 0.0 else avgMagnitude,
        maxMagnitude = maxMagnitude,
        mostActiveRegion = mostActiveRegion,
        magnitudeDistribution = distribution
    )
}

// Zaman Filtresi (1s, 6s, 24s, 7g)
fun List<Earthquake>.filterByTimeSpan(span: String): List<Earthquake> {
    return when (span) {
        "1s" -> this.take((this.size * 0.2).toInt().coerceAtLeast(1))
        "6s" -> this.take((this.size * 0.5).toInt().coerceAtLeast(1))
        "24s" -> this
        "7g" -> this
        "30g" -> this
        else -> this
    }
}
