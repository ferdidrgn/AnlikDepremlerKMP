package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorldIGPEarthquakeDto(
    @SerialName("la")
    val latitude: Double? = null,   // Enlem (latitude)
    @SerialName("lo")
    val longitude: Double? = null,   // Boylam (longitude)
    @SerialName("de")
    val depth: String? = null,   // Derinlik (depth)
    @SerialName("ma")
    val magnitude: String? = null,   // Büyüklük (magnitude)
    @SerialName("mt")
    val magnitudeType: String? = null,  // Deprem türü (magnitude type)
    @SerialName("p1")
    val p1: String? = null,  // Belirtilen bir kod (belirli bir anlamı olabilir)
    @SerialName("it")
    val it: String? = null,  // Bilinmiyor (belirtilen kullanım durumu yok)
    @SerialName("pl")
    val place: String? = null,    // Depremin gerçekleştiği yer (place)
    @SerialName("pr")
    val provider: String? = null,   // Kaynak (provider)
    @SerialName("dt")
    val dateTime: String? = null,   // Tarih ve saat (date and time)
    @SerialName("di")
    val di: String? = null,   // Bilinmiyor (belirtilen kullanım durumu yok)
    @SerialName("mr")
    val mr: String? = null,   // Bilinmiyor (belirtilen kullanım durumu yok)
    @SerialName("py")
    val py: String? = null,    // Bilinmiyor (belirtilen kullanım durumu yok)
    @SerialName("sm")
    val sm: String? = null,   // Bilinmiyor (belirtilen kullanım durumu yok)
    @SerialName("rp")
    val rp: String? = null,    // Bilinmiyor (belirtilen kullanım durumu yok)
)
