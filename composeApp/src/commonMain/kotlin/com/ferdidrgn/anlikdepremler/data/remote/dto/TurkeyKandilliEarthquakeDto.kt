package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TurkeyKandilliEarthquakeDto(
    @SerialName("date") val date: String? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("latitude") val latitude: String? = null,
    @SerialName("longitude") val longitude: String? = null,
    @SerialName("depth") val depth: String? = null,
    @SerialName("md") val md: String? = null,
    @SerialName("ml") val ml: String? = null,
    @SerialName("mw") val mw: String? = null,
    @SerialName("revize") val revize: String? = null
) : Serializable