package com.ferdidrgn.anlikdepremler.data.model

data class DisasterGuideItem(
    val id: String,
    val titleResourceKey: String,
    val descriptionResourceKey: String,
    val category: String,
    val referenceUrl: String,
    val sourceName: String
)

object OfficialDisasterGuides {
    val list = listOf(
        DisasterGuideItem(
            id = "afad_before",
            titleResourceKey = "guide_afad_before_title",
            descriptionResourceKey = "guide_afad_before_desc",
            category = "AFAD Hazırlık",
            referenceUrl = "https://www.afad.gov.tr/deprem-oncesi-ani-ve-sonrasi-alınmasi-gereken-onlemler",
            sourceName = "AFAD Resmi Web Sitesi"
        ),
        DisasterGuideItem(
            id = "akut_bag",
            titleResourceKey = "guide_akut_bag_title",
            descriptionResourceKey = "guide_akut_bag_desc",
            category = "AKUT Çanta",
            referenceUrl = "https://www.akut.org.tr/deprem-cantasi-nasil-hazirlanir",
            sourceName = "AKUT Arama Kurtarma Derneği"
        ),
        DisasterGuideItem(
            id = "kizilay_first_aid",
            titleResourceKey = "guide_kizilay_first_aid_title",
            descriptionResourceKey = "guide_kizilay_first_aid_desc",
            category = "Kızılay İlkyardım",
            referenceUrl = "https://www.kizilay.org.tr/Afet/Deprem",
            sourceName = "Türk Kızılay Afet Yönetimi"
        )
    )
}