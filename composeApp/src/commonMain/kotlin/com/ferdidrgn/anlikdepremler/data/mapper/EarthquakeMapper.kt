package com.ferdidrgn.anlikdepremler.data.mapper

import com.ferdi.deprem.model.Earthquake as DomainEarthquake
import com.ferdidrgn.anlikdepremler.data.remote.dto.*
import kotlin.random.Random

// ==========================================
// 3. TURKEY ALL (ORHAN AYDOĞDU) MAPPER
// ==========================================
fun TurkeyAllEarthquake.toDomain(): DomainEarthquake {
    val magVal = this.mag ?: 0.0
    val dateTimeParts = this.dateTime?.split(" ")

    return DomainEarthquake(
        id = this.id ?: this.earthquakeId ?: randomEarthquakeId(),
        location = this.title ?: "Bilinmeyen Konum",
        region = this.locationProperties?.closestCity?.name ?: "Türkiye",
        magnitude = magVal,
        depth = this.depth ?: 0.0,
        date = this.date ?: "",
        time = dateTimeParts?.getOrNull(1) ?: "",
        latitude = this.geojson?.coordinates?.getOrNull(1) ?: 0.0,
        longitude = this.geojson?.coordinates?.getOrNull(0) ?: 0.0,
        cityImageUrl = getRandomCityImage(),
        isSignificant = magVal >= 4.5,
        intensity = calculateIntensity(magVal)
    )
}

// ==========================================
// 4. WORLD USGS MAPPER
// ==========================================
fun UsgsFeature.toDomain(): DomainEarthquake {
    val magVal = this.properties?.mag ?: 0.0
    val coords = this.geometry?.coordinates

    return DomainEarthquake(
        id = this.id ?: randomEarthquakeId(),
        location = this.properties?.place ?: "Dünya Geneli",
        region = "GLOBAL",
        magnitude = magVal,
        depth = coords?.getOrNull(2) ?: 0.0,
        date = "Bugün",
        time = "",
        latitude = coords?.getOrNull(1) ?: 0.0,
        longitude = coords?.getOrNull(0) ?: 0.0,
        cityImageUrl = getRandomCityImage(),
        isSignificant = magVal >= 5.0,
        intensity = calculateIntensity(magVal)
    )
}

// ==========================================
// 5. WORLD IGP MAPPER
// ==========================================
fun WorldIGPEarthquakeDto.toDomain(): DomainEarthquake {
    val magVal = this.magnitude?.toDoubleOrNull() ?: 0.0
    val dateTimeParts = this.dateTime?.split(" ")

    return DomainEarthquake(
        id = randomEarthquakeId(),
        location = this.place ?: "Dünya Geneli",
        region = "DÜNYA",
        magnitude = magVal,
        depth = this.depth?.toDoubleOrNull() ?: 0.0,
        date = dateTimeParts?.getOrNull(0) ?: "",
        time = dateTimeParts?.getOrNull(1) ?: "",
        latitude = this.latitude ?: 0.0,
        longitude = this.longitude ?: 0.0,
        cityImageUrl = getRandomCityImage(),
        isSignificant = magVal >= 5.0,
        intensity = calculateIntensity(magVal)
    )
}

fun EmscFeature.toDomain(): DomainEarthquake {
    val magVal = this.properties?.mag ?: 0.0
    val coords = this.geometry?.coordinates
    val timeParts = this.properties?.time?.split("T")

    return DomainEarthquake(
        id = this.id ?: this.properties?.sourceId ?: randomEarthquakeId(),
        location = this.properties?.flynnRegion ?: "Avrupa / Dünya",
        region = "EMSC - EU",
        magnitude = magVal,
        depth = this.properties?.depth ?: coords?.getOrNull(2) ?: 0.0,
        date = timeParts?.getOrNull(0) ?: "",
        time = timeParts?.getOrNull(1)?.take(5) ?: "",
        latitude = coords?.getOrNull(1) ?: 0.0,
        longitude = coords?.getOrNull(0) ?: 0.0,
        cityImageUrl = getRandomCityImage(),
        isSignificant = magVal >= 4.5,
        intensity = calculateIntensity(magVal),
        source = "EMSC"
    )
}

// ==========================================
// YARDIMCI METOTLAR
// ==========================================
private fun calculateIntensity(magnitude: Double): String {
    return when {
        magnitude >= 7.0 -> "X+"
        magnitude >= 6.0 -> "VIII"
        magnitude >= 5.0 -> "VI"
        magnitude >= 4.0 -> "IV"
        else -> "II"
    }
}

private fun getRandomCityImage(): String {
    return "https://picsum.photos/400/250?random=${(1..100).random()}"
}

private fun randomEarthquakeId(): String = Random.nextLong().toString()