package com.ferdi.deprem.model

data class Earthquake(
    val id: String,
    val location: String,
    val region: String = "Marmara",
    val magnitude: Double,
    val depth: Double,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val cityImageUrl: String,
    val isSignificant: Boolean = false,
    val intensity: String = "IV",
    val source: String = "AFAD"
)

data class EarthquakeStatistics(
    val totalToday: Int,
    val totalWeek: Int,
    val totalMonth: Int,
    val avgMagnitude: Double,
    val maxMagnitude: Double,
    val mostActiveRegion: String,
    val magnitudeDistribution: Map<String, Int>
)

data class InfoCardItem(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String
)