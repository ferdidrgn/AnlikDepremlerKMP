package com.ferdidrgn.anlikdepremler.data.mapper

import com.ferdi.deprem.model.Earthquake as DomainEarthquake
import com.ferdidrgn.anlikdepremler.data.remote.dto.EmscFeature
import com.ferdidrgn.anlikdepremler.data.remote.dto.TurkeyAllEarthquake
import com.ferdidrgn.anlikdepremler.data.remote.dto.UsgsFeature
import com.ferdidrgn.anlikdepremler.data.remote.dto.WorldIGPEarthquakeDto
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun TurkeyAllEarthquake.toDomain(): DomainEarthquake {
    val magnitudeValue = mag ?: 0.0
    val dateTimeParts = dateTime?.split(" ")
    val coordinates = geojson?.coordinates

    return DomainEarthquake(
        id = id ?: earthquakeId ?: stableEarthquakeId(dateTime, coordinates),
        location = title ?: "Bilinmeyen konum",
        region = locationProperties?.closestCity?.name ?: "Türkiye",
        magnitude = magnitudeValue,
        depth = depth ?: 0.0,
        date = date ?: dateTimeParts?.getOrNull(0) ?: "",
        time = dateTimeParts?.getOrNull(1) ?: "",
        latitude = coordinates?.getOrNull(1) ?: 0.0,
        longitude = coordinates?.getOrNull(0) ?: 0.0,
        cityImageUrl = "",
        isSignificant = magnitudeValue >= 4.5,
        intensity = "—",
        source = provider?.uppercase() ?: "TÜRKİYE"
    )
}

fun UsgsFeature.toDomain(): DomainEarthquake {
    val magnitudeValue = properties?.mag ?: 0.0
    val coordinates = geometry?.coordinates
    val localDateTime = properties?.time?.let { epochMillis ->
        Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }

    return DomainEarthquake(
        id = id ?: stableEarthquakeId(properties?.time?.toString(), coordinates),
        location = properties?.place ?: "Dünya geneli",
        region = "GLOBAL",
        magnitude = magnitudeValue,
        depth = coordinates?.getOrNull(2) ?: 0.0,
        date = localDateTime?.date?.toString() ?: "",
        time = localDateTime?.time?.toString()?.take(8) ?: "",
        latitude = coordinates?.getOrNull(1) ?: 0.0,
        longitude = coordinates?.getOrNull(0) ?: 0.0,
        cityImageUrl = "",
        isSignificant = magnitudeValue >= 5.0,
        intensity = properties?.mmi?.let { "MMI ${it.toInt()}" } ?: "—",
        source = "USGS"
    )
}

fun WorldIGPEarthquakeDto.toDomain(): DomainEarthquake {
    val magnitudeValue = magnitude?.toDoubleOrNull() ?: 0.0
    val dateTimeParts = dateTime?.split(" ")
    val coordinates = listOf(longitude ?: 0.0, latitude ?: 0.0)

    return DomainEarthquake(
        id = stableEarthquakeId(dateTime, coordinates),
        location = place ?: "Dünya geneli",
        region = "DÜNYA",
        magnitude = magnitudeValue,
        depth = depth?.toDoubleOrNull() ?: 0.0,
        date = dateTimeParts?.getOrNull(0) ?: "",
        time = dateTimeParts?.getOrNull(1) ?: "",
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        cityImageUrl = "",
        isSignificant = magnitudeValue >= 5.0,
        intensity = "—",
        source = "IGP"
    )
}

fun EmscFeature.toDomain(): DomainEarthquake {
    val magnitudeValue = properties?.mag ?: 0.0
    val coordinates = geometry?.coordinates
    val localDateTime = properties?.time?.let { raw ->
        runCatching { Instant.parse(raw).toLocalDateTime(TimeZone.currentSystemDefault()) }.getOrNull()
    }

    return DomainEarthquake(
        id = id ?: properties?.sourceId ?: stableEarthquakeId(properties?.time, coordinates),
        location = properties?.flynnRegion ?: "Avrupa / Dünya",
        region = "EMSC - EU",
        magnitude = magnitudeValue,
        depth = properties?.depth ?: coordinates?.getOrNull(2) ?: 0.0,
        date = localDateTime?.date?.toString() ?: "",
        time = localDateTime?.time?.toString()?.take(8) ?: "",
        latitude = coordinates?.getOrNull(1) ?: 0.0,
        longitude = coordinates?.getOrNull(0) ?: 0.0,
        cityImageUrl = "",
        isSignificant = magnitudeValue >= 4.5,
        intensity = "—",
        source = "EMSC"
    )
}

private fun stableEarthquakeId(timestamp: String?, coordinates: List<Double>?): String =
    listOf(timestamp.orEmpty(), coordinates?.joinToString(",").orEmpty())
        .joinToString("|")
        .hashCode()
        .toString()
