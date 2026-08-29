package com.ferdidrgn.anlikdepremler.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager(
    private val dataStore: DataStore<Preferences>
) {
    private val SELECTED_SOURCE_KEY = stringPreferencesKey("selected_source")
    private val APP_THEME_KEY = stringPreferencesKey("app_theme")
    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    private val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")

    private val EMERGENCY_PHONE_KEY = stringPreferencesKey("emergency_phone_number")

    // --- DEPREM VERİ KAYNAĞI ---
    val selectedSource: Flow<String> = dataStore.data.map { prefs ->
        prefs[SELECTED_SOURCE_KEY] ?: "KANDILLI"
    }

    suspend fun saveSelectedSource(source: String) {
        dataStore.edit { prefs -> prefs[SELECTED_SOURCE_KEY] = source }
    }

    // --- DİL SEÇİMİ ---
    val selectedLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[SELECTED_LANGUAGE_KEY] ?: "tr"
    }

    suspend fun saveSelectedLanguage(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[SELECTED_LANGUAGE_KEY] = languageCode
        }
    }

    // --- TEMA SEÇİMİ ---
    val appTheme: Flow<String> = dataStore.data.map { prefs ->
        prefs[APP_THEME_KEY] ?: "CREAM_LIGHT"
    }

    suspend fun saveAppTheme(theme: String) {
        dataStore.edit { prefs -> prefs[APP_THEME_KEY] = theme }
    }

    // SettingsViewModel ile geriye dönük uyumluluk (Eski kodların patlamaması için)
    val selectedThemeMode: Flow<String> get() = appTheme

    suspend fun saveSelectedThemeMode(themeKey: String) {
        saveAppTheme(themeKey)
    }

    // --- ONBOARDING TAMAMLANMA DURUMU ---
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETED_KEY] = completed }
    }

    val emergencyPhoneNumber: Flow<String> = dataStore.data.map { prefs ->
        prefs[EMERGENCY_PHONE_KEY] ?: ""
    }

    suspend fun saveEmergencyPhoneNumber(phoneNumber: String) {
        dataStore.edit { prefs ->
            prefs[EMERGENCY_PHONE_KEY] = phoneNumber
        }
    }
}
