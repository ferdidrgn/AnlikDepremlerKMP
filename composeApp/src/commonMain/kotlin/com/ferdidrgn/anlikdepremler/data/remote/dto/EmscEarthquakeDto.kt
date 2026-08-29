package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmscEarthquakeDto(
    @SerialName("type") val type: String? = null,
    @SerialName("features") val features: List<Feature>? = null
) {

    @Serializable
    data class Feature(
        @SerialName("type") val type: String? = null,
        @SerialName("id") val id: String? = null,
        @SerialName("geometry") val geometry: Geometry? = null,
        @SerialName("properties") val properties: Properties? = null
    )

    @Serializable
    data class Geometry(
        @SerialName("type") val type: String? = null,
        @SerialName("coordinates") val coordinates: List<Double>? = null // [longitude, latitude, depth]
    )

    @Serializable
    data class Properties(
        @SerialName("flynn_region") val flynnRegion: String? = null,
        @SerialName("time") val time: String? = null,
        @SerialName("mag") val mag: Double? = null,
        @SerialName("magtype") val magType: String? = null,
        @SerialName("depth") val depth: Double? = null,
        @SerialName("source_id") val sourceId: String? = null
    )
}