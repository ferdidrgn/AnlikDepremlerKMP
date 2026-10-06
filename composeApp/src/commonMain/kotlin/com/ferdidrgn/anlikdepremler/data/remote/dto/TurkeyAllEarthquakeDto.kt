package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TurkeyAllEarthquakeDto(
    @SerialName("status") val status: Boolean? = null,
    @SerialName("httpStatus") val httpStatus: Int? = null,
    @SerialName("serverloadms") val serverloadms: Int? = null,
    @SerialName("desc") val desc: String? = null,
    @SerialName("metadata") val metadata: TurkeyAllMetadata? = null,
    @SerialName("result") val result: List<TurkeyAllEarthquake>? = null
)

// Kept top-level rather than nested inside TurkeyAllEarthquakeDto - a @Serializable class nested
// inside another @Serializable class crashes the Kotlin/Wasm compiler's K2 serialization plugin
// (internal NPE in SerializationFirUtilsKt.createDeprecatedHiddenAnnotation while generating the
// nested class's serializer). Nesting has no effect on the JSON mapping itself, which matches by
// @SerialName, so flattening these out is a pure, behavior-preserving workaround.
@Serializable
data class TurkeyAllMetadata(
    @SerialName("date_starts") val dateStarts: String? = null,
    @SerialName("date_ends") val dateEnds: String? = null,
    @SerialName("total") val total: Int? = null
)

@Serializable
data class TurkeyAllEarthquake(
    @SerialName("_id") val id: String? = null,
    @SerialName("earthquake_id") val earthquakeId: String? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("mag") val mag: Double? = null,
    @SerialName("depth") val depth: Double? = null,
    @SerialName("geojson") val geojson: TurkeyAllGeoJson? = null,
    @SerialName("location_properties") val locationProperties: TurkeyAllLocationProperties? = null,
    @SerialName("rev") val rev: String? = null,
    @SerialName("date_time") val dateTime: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("location_tz") val locationTz: String? = null
)

@Serializable
data class TurkeyAllGeoJson(
    @SerialName("type") val type: String? = null,
    @SerialName("coordinates") val coordinates: List<Double>? = null
)

@Serializable
data class TurkeyAllLocationProperties(
    @SerialName("closestCity") val closestCity: TurkeyAllCity? = null,
    @SerialName("epiCenter") val epiCenter: TurkeyAllCity? = null,
    @SerialName("closestCities") val closestCities: List<TurkeyAllCity>? = null,
    @SerialName("airports") val airports: List<TurkeyAllAirport>? = null
)

@Serializable
data class TurkeyAllCity(
    @SerialName("name") val name: String? = null,
    @SerialName("cityCode") val cityCode: Int? = null,
    @SerialName("distance") val distance: Double? = null,
    @SerialName("population") val population: Int? = null
)

@Serializable
data class TurkeyAllAirport(
    @SerialName("distance") val distance: Double? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("coordinates") val coordinates: TurkeyAllGeoJson? = null
)
