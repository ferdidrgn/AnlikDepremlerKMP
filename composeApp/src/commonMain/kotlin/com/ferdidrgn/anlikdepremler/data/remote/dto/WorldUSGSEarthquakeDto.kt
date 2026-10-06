package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorldUSGSEarthquakeDto(
    @SerialName("type") val type: String? = null,
    @SerialName("metadata") val metadata: UsgsMetadata? = null,
    @SerialName("features") val features: List<UsgsFeature>? = null,
    @SerialName("bbox") val bbox: List<Double>? = null
)

// Kept top-level rather than nested inside WorldUSGSEarthquakeDto - a @Serializable class nested
// inside another @Serializable class crashes the Kotlin/Wasm compiler's K2 serialization plugin
// (internal NPE in SerializationFirUtilsKt.createDeprecatedHiddenAnnotation while generating the
// nested class's serializer). Nesting has no effect on the JSON mapping itself, which matches by
// @SerialName, so flattening these out is a pure, behavior-preserving workaround.
@Serializable
data class UsgsMetadata(
    @SerialName("generated") val generated: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("status") val status: Int? = null,
    @SerialName("api") val api: String? = null,
    @SerialName("count") val count: Int? = null
)

@Serializable
data class UsgsFeature(
    @SerialName("type") val type: String? = null,
    @SerialName("properties") val properties: UsgsProperties? = null,
    @SerialName("geometry") val geometry: UsgsGeometry? = null,
    @SerialName("id") val id: String? = null
)

@Serializable
data class UsgsProperties(
    @SerialName("mag") val mag: Double? = null,
    @SerialName("place") val place: String? = null,
    @SerialName("time") val time: Long? = null,
    @SerialName("updated") val updated: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("detail") val detail: String? = null,
    @SerialName("felt") val felt: Int? = null,
    @SerialName("cdi") val cdi: Double? = null,
    @SerialName("mmi") val mmi: Double? = null,
    @SerialName("alert") val alert: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("tsunami") val tsunami: Byte? = null,
    @SerialName("sig") val sig: Int? = null,
    @SerialName("net") val net: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("ids") val ids: String? = null,
    @SerialName("sources") val sources: String? = null,
    @SerialName("types") val types: String? = null,
    @SerialName("nst") val nst: Int? = null,
    @SerialName("dmin") val dmin: Double? = null,
    @SerialName("rms") val rms: Double? = null,
    @SerialName("gap") val gap: Double? = null,
    @SerialName("magType") val magType: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("title") val title: String? = null
)

@Serializable
data class UsgsGeometry(
    @SerialName("type") val type: String? = null,
    @SerialName("coordinates") val coordinates: List<Double>? = null
)
