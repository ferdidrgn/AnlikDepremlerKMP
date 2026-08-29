package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TurkeyAfadEarthquakeDto(
    @SerialName("rms") val rms: String? = null,
    @SerialName("eventID") val eventID: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("latitude") val latitude: String? = null,
    @SerialName("longitude") val longitude: String? = null,
    @SerialName("depth") val depth: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("magnitude") val magnitude: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("province") val province: String? = null,
    @SerialName("district") val district: String? = null,
    @SerialName("neighborhood") val neighborhood: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("isEventUpdate") val isEventUpdate: Boolean? = null,
    @SerialName("lastUpdateDate") val lastUpdateDate: String? = null
) : Serializable