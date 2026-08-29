package com.ferdidrgn.anlikdepremler.data.model

data class HistoricalEarthquake(
    val id: String,
    val title: String,
    val date: String,
    val magnitude: Double,
    val depth: String,
    val location: String,
    val description: String,
    val casualtiesText: String
)

object HistoricalEarthquakesData {
    val list = listOf(
        HistoricalEarthquake(
            id = "kahramanmaras_2023",
            title = "Kahramanmaraş - Pazarcık Depremi",
            date = "6 Şubat 2023 • 04:17",
            magnitude = 7.8,
            depth = "8.6 km",
            location = "Kahramanmaraş, Türkiye",
            description = "11 ili etkileyen, Asrın Felaketi olarak adlandırılan ve 350.000 km²\'lik alanda yıkıma yol açan büyük deprem serisi.",
            casualtiesText = "Mw 7.8 & 7.6 Çifte Deprem"
        ),
        HistoricalEarthquake(
            id = "izmir_2020",
            title = "Ege Denizi - Seferihisar Depremi",
            date = "30 Ekim 2020 • 14:51",
            magnitude = 6.9,
            depth = "16.5 km",
            location = "İzmir / Ege Denizi",
            description = "Seferihisar açıklarında meydana gelen ve küçük çaplı tsunamiye yol açan, Bayraklı ilçesinde yıkımlara neden olan sarsıntı.",
            casualtiesText = "Mw 6.9 Tsunami Etkili"
        ),
        HistoricalEarthquake(
            id = "van_2011",
            title = "Van - Tabanlı Depremi",
            date = "23 Ekim 2011 • 13:41",
            magnitude = 7.2,
            depth = "19.0 km",
            location = "Van, Türkiye",
            description = "Van ve Erciş ilçesinde şiddetli yıkıma yol açan, 25 saniye süren yıkıcı sismik hareketlilik.",
            casualtiesText = "Mw 7.2 Şiddetli Yıkım"
        ),
        HistoricalEarthquake(
            id = "golcuk_1999",
            title = "Marmara / Gölcük Depremi",
            date = "17 Ağustos 1999 • 03:02",
            magnitude = 7.6,
            depth = "17.0 km",
            location = "Kocaeli / Gölcük",
            description = "Tüm Marmara bölgesini derinden sarsan, 45 saniye süren Türkiye yakın tarihinin en büyük afetlerinden biri.",
            casualtiesText = "Mw 7.6 Kocaeli Afeti"
        )
    )
}