package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorldUSGSEarthquakeDto(
    @SerialName("type") val type: String? = null,
    @SerialName("metadata") val metadata: Metadata? = null,
    @SerialName("features") val features: List<Feature>? = null,
    @SerialName("bbox") val bbox: List<Double>? = null
) {

    @Serializable
    data class Metadata(
        @SerialName("generated") val generated: Long? = null,
        @SerialName("url") val url: String? = null,
        @SerialName("title") val title: String? = null,
        @SerialName("status") val status: Int? = null,
        @SerialName("api") val api: String? = null,
        @SerialName("count") val count: Int? = null
    )

    @Serializable
    data class Feature(
        @SerialName("type") val type: String? = null,
        @SerialName("properties") val properties: Properties? = null,
        @SerialName("geometry") val geometry: Geometry? = null,
        @SerialName("id") val id: String? = null
    ) {
        @Serializable
        data class Properties(
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
        data class Geometry(
            @SerialName("type") val type: String? = null,
            @SerialName("coordinates") val coordinates: List<Double>? = null
        )
    }
}