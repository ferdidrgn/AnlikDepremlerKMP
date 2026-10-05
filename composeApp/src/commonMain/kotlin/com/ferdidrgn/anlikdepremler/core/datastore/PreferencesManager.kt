package com.ferdidrgn.anlikdepremler.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.ferdidrgn.anlikdepremler.core.util.EarthquakeJournalEntry
import com.ferdidrgn.anlikdepremler.core.util.SavedLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    private val SAVED_LOCATIONS_JSON_KEY = stringPreferencesKey("saved_locations_json")
    private val SHAKE_DETECTION_ENABLED_KEY = booleanPreferencesKey("shake_detection_enabled")
    private val SHAKE_SENSITIVITY_KEY = floatPreferencesKey("shake_sensitivity")
    private val EARTHQUAKE_JOURNAL_JSON_KEY = stringPreferencesKey("earthquake_journal_json")
    private val ADS_FREE_UNTIL_MILLIS_KEY = longPreferencesKey("ads_free_until_millis")
    private val COMPLETED_CHECKLIST_ITEMS_KEY = stringSetPreferencesKey("completed_checklist_items")
    private val WEEKLY_DIGEST_ENABLED_KEY = booleanPreferencesKey("weekly_digest_enabled")
    private val FELT_REPORTED_EARTHQUAKE_IDS_KEY = stringSetPreferencesKey("felt_reported_earthquake_ids")
    private val EXTRA_SAVED_LOCATION_SLOTS_KEY = intPreferencesKey("extra_saved_location_slots")

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

    // --- KAYITLI KONUMLAR (ev/iş/aile) - nearby-deprem kontrolü GPS'e ek olarak bunları da tarar ---
    val savedLocations: Flow<List<SavedLocation>> = dataStore.data.map { prefs ->
        val json = prefs[SAVED_LOCATIONS_JSON_KEY]
        if (json == null) {
            emptyList()
        } else {
            runCatching { Json.decodeFromString<List<SavedLocation>>(json) }.getOrDefault(emptyList())
        }
    }

    suspend fun saveSavedLocations(locations: List<SavedLocation>) {
        dataStore.edit { prefs -> prefs[SAVED_LOCATIONS_JSON_KEY] = Json.encodeToString(locations) }
    }

    // --- "HİSSETTİM" RAPORU VERİLMİŞ DEPREM ID'LERİ - cihaz bazlı tekrar oy vermeyi engeller ---
    // (Firestore'daki gerçek sayaç herkese açık/anonim; bu sadece "bu cihaz zaten oy verdi mi" kaydı)
    val feltReportedEarthquakeIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[FELT_REPORTED_EARTHQUAKE_IDS_KEY] ?: emptySet()
    }

    suspend fun markEarthquakeAsFelt(earthquakeId: String) {
        dataStore.edit { prefs ->
            val current = prefs[FELT_REPORTED_EARTHQUAKE_IDS_KEY] ?: emptySet()
            prefs[FELT_REPORTED_EARTHQUAKE_IDS_KEY] = current + earthquakeId
        }
    }

    // --- SARSINTI ALGILAMA (deneysel, cihaz ivmeölçeri ile, varsayılan kapalı) ---
    val shakeDetectionEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHAKE_DETECTION_ENABLED_KEY] ?: false
    }

    suspend fun saveShakeDetectionEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SHAKE_DETECTION_ENABLED_KEY] = enabled }
    }

    /** G-force multiplier threshold - lower is more sensitive (more false positives from normal
     *  handling), higher means only a genuinely violent shake triggers it. */
    val shakeSensitivity: Flow<Float> = dataStore.data.map { prefs ->
        prefs[SHAKE_SENSITIVITY_KEY] ?: 2.7f
    }

    suspend fun saveShakeSensitivity(value: Float) {
        dataStore.edit { prefs -> prefs[SHAKE_SENSITIVITY_KEY] = value }
    }

    // --- DEPREM GÜNLÜĞÜ - "hissettim" işaretlenen depremlerin kişisel, sadece cihazda kaydı ---
    val earthquakeJournal: Flow<List<EarthquakeJournalEntry>> = dataStore.data.map { prefs ->
        val json = prefs[EARTHQUAKE_JOURNAL_JSON_KEY]
        if (json == null) {
            emptyList()
        } else {
            runCatching { Json.decodeFromString<List<EarthquakeJournalEntry>>(json) }.getOrDefault(emptyList())
        }
    }

    suspend fun addJournalEntry(entry: EarthquakeJournalEntry) {
        dataStore.edit { prefs ->
            val current = prefs[EARTHQUAKE_JOURNAL_JSON_KEY]
                ?.let { runCatching { Json.decodeFromString<List<EarthquakeJournalEntry>>(it) }.getOrDefault(emptyList()) }
                ?: emptyList()
            if (current.any { it.earthquakeId == entry.earthquakeId }) return@edit
            prefs[EARTHQUAKE_JOURNAL_JSON_KEY] = Json.encodeToString(current + entry)
        }
    }

    // --- REKLAMSIZ DÖNEM (6 aylık "Reklamları Kaldır" satın alımıyla) ---
    val adsFreeUntilMillis: Flow<Long> = dataStore.data.map { prefs ->
        prefs[ADS_FREE_UNTIL_MILLIS_KEY] ?: 0L
    }

    /** Extends from the later of (now, current expiry) by 6 calendar months, so repurchasing
     *  before the current period ends stacks on top of it instead of resetting the clock. */
    suspend fun extendAdsFreeBySixMonths() {
        val now = Clock.System.now()
        val currentUntilMillis = adsFreeUntilMillis.first()
        val baseInstant = if (currentUntilMillis > now.toEpochMilliseconds()) {
            Instant.fromEpochMilliseconds(currentUntilMillis)
        } else {
            now
        }
        val timeZone = TimeZone.currentSystemDefault()
        val newDate = baseInstant.toLocalDateTime(timeZone).date.plus(6, DateTimeUnit.MONTH)
        val newUntilMillis = newDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
        dataStore.edit { prefs -> prefs[ADS_FREE_UNTIL_MILLIS_KEY] = newUntilMillis }
    }

    // --- ACİL DURUM ÇANTASI KONTROL LİSTESİ - tamamlanan öğelerin kaydı ---
    val completedChecklistItems: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[COMPLETED_CHECKLIST_ITEMS_KEY] ?: emptySet()
    }

    suspend fun setChecklistItemCompleted(itemId: String, completed: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[COMPLETED_CHECKLIST_ITEMS_KEY] ?: emptySet()
            prefs[COMPLETED_CHECKLIST_ITEMS_KEY] = if (completed) current + itemId else current - itemId
        }
    }

    // --- HAFTALIK DEPREM ÖZETİ BİLDİRİMİ (varsayılan kapalı) ---
    val weeklyDigestEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[WEEKLY_DIGEST_ENABLED_KEY] ?: false
    }

    suspend fun saveWeeklyDigestEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[WEEKLY_DIGEST_ENABLED_KEY] = enabled }
    }

    // --- ÖDÜLLÜ REKLAM İLE KAZANILAN EK KAYITLI KONUM HAKKI ---
    val extraSavedLocationSlots: Flow<Int> = dataStore.data.map { prefs ->
        prefs[EXTRA_SAVED_LOCATION_SLOTS_KEY] ?: 0
    }

    suspend fun grantExtraSavedLocationSlot() {
        dataStore.edit { prefs ->
            val current = prefs[EXTRA_SAVED_LOCATION_SLOTS_KEY] ?: 0
            prefs[EXTRA_SAVED_LOCATION_SLOTS_KEY] = current + 1
        }
    }
}
