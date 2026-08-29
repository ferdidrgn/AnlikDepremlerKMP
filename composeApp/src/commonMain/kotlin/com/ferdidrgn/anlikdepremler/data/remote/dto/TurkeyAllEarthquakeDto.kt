package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TurkeyAllEarthquakeDto(
    @SerialName("status") val status: Boolean? = null,
    @SerialName("httpStatus") val httpStatus: Int? = null,
    @SerialName("serverloadms") val serverloadms: Int? = null,
    @SerialName("desc") val desc: String? = null,
    @SerialName("metadata") val metadata: Metadata? = null,
    @SerialName("result") val result: List<Earthquake>? = null
) {

    @Serializable
    data class Metadata(
        @SerialName("date_starts") val dateStarts: String? = null,
        @SerialName("date_ends") val dateEnds: String? = null,
        @SerialName("total") val total: Int? = null
    )

    @Serializable
    data class Earthquake(
        @SerialName("_id") val id: String? = null,
        @SerialName("earthquake_id") val earthquakeId: String? = null,
        @SerialName("provider") val provider: String? = null,
        @SerialName("title") val title: String? = null,
        @SerialName("date") val date: String? = null,
        @SerialName("mag") val mag: Double? = null,
        @SerialName("depth") val depth: Double? = null,
        @SerialName("geojson") val geojson: GeoJson? = null,
        @SerialName("location_properties") val locationProperties: LocationProperties? = null,
        @SerialName("rev") val rev: String? = null,
        @SerialName("date_time") val dateTime: String? = null,
        @SerialName("created_at") val createdAt: Long? = null,
        @SerialName("location_tz") val locationTz: String? = null
    )

    @Serializable
    data class GeoJson(
        @SerialName("type") val type: String? = null,
        @SerialName("coordinates") val coordinates: List<Double>? = null
    )

    @Serializable
    data class LocationProperties(
        @SerialName("closestCity") val closestCity: City? = null,
        @SerialName("epiCenter") val epiCenter: City? = null,
        @SerialName("closestCities") val closestCities: List<City>? = null,
        @SerialName("airports") val airports: List<Airport>? = null
    )

    @Serializable
    data class City(
        @SerialName("name") val name: String? = null,
        @SerialName("cityCode") val cityCode: Int? = null,
        @SerialName("distance") val distance: Double? = null,
        @SerialName("population") val population: Int? = null
    )

    @Serializable
    data class Airport(
        @SerialName("distance") val distance: Double? = null,
        @SerialName("name") val name: String? = null,
        @SerialName("code") val code: String? = null,
        @SerialName("coordinates") val coordinates: GeoJson? = null
    )
}