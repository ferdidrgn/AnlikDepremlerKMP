package com.ferdidrgn.anlikdepremler.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorldGeneralEarthquakeDto(
    @SerialName("la") val latitude: String? = null, // Depremin enlemi
    @SerialName("lo") val longitude: String? = null, // Depremin boylamı
    @SerialName("de") val depth: String? = null, // Depremin derinliği (km cinsinden)
    @SerialName("ma") val magnitude: String? = null, // Depremin büyüklüğü
    @SerialName("mt") val magnitudeType: String? = null, // Büyüklük ölçüm türü (örn: ml, m, mb)
    @SerialName("p1") val locationCode: String? = null, // Konum kodu
    @SerialName("it") val isTsunami: String? = null, // Tsunami oluşumu (1 ise evet, 0 ise hayır)
    @SerialName("pl") val locationName: String? = null, // Depremin meydana geldiği yerin adı
    @SerialName("pr") val provider: String? = null, // Deprem bilgi sağlayıcısı
    @SerialName("dt") val dateTime: String? = null, // Depremin tarihi ve saati
    @SerialName("di") val distance: String? = null, // Depremden bir referans noktasına olan mesafe
    @SerialName("mr") val isReported: String? = null, // Bildirilen büyüklük durumu (1 ise bildirildi, 0 ise bildirilmedi)
    @SerialName("py") val isAftershock: String? = null, // Artçı şok oluşumu (1 ise evet, 0 ise hayır)
    @SerialName("sm") val shakingMap: String? = null, // Sarsıntı haritası (0 ise mevcut değil)
    @SerialName("rp") val isReviewed: String? = null, // İnceleme durumu (1 ise incelendi, 0 ise incelenmedi)
)
