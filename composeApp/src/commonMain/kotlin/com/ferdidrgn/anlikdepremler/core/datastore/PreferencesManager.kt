package com.ferdidrgn.anlikdepremler.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
    private val NEARBY_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("nearby_notifications_enabled")
    private val VOICE_ALERTS_ENABLED_KEY = booleanPreferencesKey("voice_alerts_enabled")
    private val MIN_MAGNITUDE_THRESHOLD_KEY = floatPreferencesKey("min_magnitude_threshold")
    private val MAX_DISTANCE_KM_KEY = floatPreferencesKey("max_distance_km")
    private val QUIET_HOURS_ENABLED_KEY = booleanPreferencesKey("quiet_hours_enabled")
    private val QUIET_HOURS_START_HOUR_KEY = intPreferencesKey("quiet_hours_start_hour")
    private val QUIET_HOURS_END_HOUR_KEY = intPreferencesKey("quiet_hours_end_hour")

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

    // --- YAKIN DEPREM BİLDİRİMLERİ (AÇIK/KAPALI) ---
    val nearbyNotificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NEARBY_NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    suspend fun saveNearbyNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[NEARBY_NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    // --- SESLİ UYARI (TTS) AÇIK/KAPALI - varsayılan kapalı, beklenmedik sesli anonsu tercihe bırakır ---
    val voiceAlertsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[VOICE_ALERTS_ENABLED_KEY] ?: false
    }

    suspend fun saveVoiceAlertsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[VOICE_ALERTS_ENABLED_KEY] = enabled }
    }

    // --- BİLDİRİM FİLTRELERİ: minimum büyüklük, maksimum mesafe, sessiz saatler ---
    val minMagnitudeThreshold: Flow<Float> = dataStore.data.map { prefs ->
        prefs[MIN_MAGNITUDE_THRESHOLD_KEY] ?: 4.0f
    }

    suspend fun saveMinMagnitudeThreshold(value: Float) {
        dataStore.edit { prefs -> prefs[MIN_MAGNITUDE_THRESHOLD_KEY] = value }
    }

    val maxDistanceKm: Flow<Float> = dataStore.data.map { prefs ->
        prefs[MAX_DISTANCE_KM_KEY] ?: 100.0f
    }

    suspend fun saveMaxDistanceKm(value: Float) {
        dataStore.edit { prefs -> prefs[MAX_DISTANCE_KM_KEY] = value }
    }

    val quietHoursEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[QUIET_HOURS_ENABLED_KEY] ?: false
    }

    suspend fun saveQuietHoursEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[QUIET_HOURS_ENABLED_KEY] = enabled }
    }

    /** 0-23, local hour the quiet window starts (e.g. 22 for 22:00). */
    val quietHoursStartHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[QUIET_HOURS_START_HOUR_KEY] ?: 22
    }

    suspend fun saveQuietHoursStartHour(hour: Int) {
        dataStore.edit { prefs -> prefs[QUIET_HOURS_START_HOUR_KEY] = hour }
    }

    /** 0-23, local hour the quiet window ends (e.g. 7 for 07:00). */
    val quietHoursEndHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[QUIET_HOURS_END_HOUR_KEY] ?: 7
    }

    suspend fun saveQuietHoursEndHour(hour: Int) {
        dataStore.edit { prefs -> prefs[QUIET_HOURS_END_HOUR_KEY] = hour }
    }
}
