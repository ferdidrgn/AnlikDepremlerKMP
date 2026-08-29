package com.ferdidrgn.anlikdepremler.core.language

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    TURKISH("tr", "Türkçe", "🇹🇷"),
    ENGLISH("en", "English", "🇬🇧"),
    GERMAN("de", "Deutsch", "🇩🇪"),
    SPANISH("es", "Español", "🇪🇸"),
    ITALIAN("it", "Italiano", "🇮🇹"),
    RUSSIAN("ru", "Русский", "🇷🇺"),
    UKRAINIAN("uk", "Українська", "🇺🇦"),
    GREEK("el", "Ελληνικά", "🇬🇷"),
    KYRGYZ("ky", "Кыргызча", "🇰🇬"),
    UZBEK("uz", "Oʻzbekcha", "🇺🇿"),
    ARABIC("ar", "العربية", "🇸🇦"),
    KOREAN("ko", "한국어", "🇰🇷"),
    JAPANESE("ja", "日本語", "🇯🇵"),
    CHINESE("zh", "中文", "🇨🇳");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: TURKISH
        }
    }
}