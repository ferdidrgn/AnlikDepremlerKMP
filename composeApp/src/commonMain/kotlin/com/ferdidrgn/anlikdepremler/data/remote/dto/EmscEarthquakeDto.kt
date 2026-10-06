package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmscEarthquakeDto(
    @SerialName("type") val type: String? = null,
    @SerialName("features") val features: List<EmscFeature>? = null
)

// Kept top-level rather than nested inside EmscEarthquakeDto - a @Serializable class nested
// inside another @Serializable class crashes the Kotlin/Wasm compiler's K2 serialization plugin
// (internal NPE in SerializationFirUtilsKt.createDeprecatedHiddenAnnotation while generating the
// nested class's serializer). Nesting has no effect on the JSON mapping itself, which matches by
// @SerialName, so flattening these out is a pure, behavior-preserving workaround.
@Serializable
data class EmscFeature(
    @SerialName("type") val type: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("geometry") val geometry: EmscGeometry? = null,
    @SerialName("properties") val properties: EmscProperties? = null
)

@Serializable
data class EmscGeometry(
    @SerialName("type") val type: String? = null,
    @SerialName("coordinates") val coordinates: List<Double>? = null // [longitude, latitude, depth]
)

@Serializable
data class EmscProperties(
    @SerialName("flynn_region") val flynnRegion: String? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("mag") val mag: Double? = null,
    @SerialName("magtype") val magType: String? = null,
    @SerialName("depth") val depth: Double? = null,
    @SerialName("source_id") val sourceId: String? = null
)
