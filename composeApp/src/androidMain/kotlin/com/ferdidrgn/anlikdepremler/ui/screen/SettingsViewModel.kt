package com.ferdidrgn.anlikdepremler.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.core.language.AppLanguage
import com.ferdidrgn.anlikdepremler.core.util.LocaleUtils
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class SendEmail(val email: String) : SettingsEvent
    object OpenNotificationSettings : SettingsEvent
    object OpenLocationSettings : SettingsEvent
    object RequestReview : SettingsEvent
    object ShareApp : SettingsEvent
    data class NavigateToWeb(val url: String) : SettingsEvent
    data class BuyCoffee(val productId: String) : SettingsEvent
}

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // --- DİL VE TEMA STATE'LERİ ---
    val currentLanguage: StateFlow<AppLanguage> = preferencesManager.selectedLanguage.map {
        AppLanguage.fromCode(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.TURKISH)

    val currentTheme: StateFlow<AppThemeMode> =
        preferencesManager.selectedThemeMode.map { modeName ->
            try {
                AppThemeMode.valueOf(modeName)
            } catch (e: Exception) {
                AppThemeMode.CREAM_LIGHT
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.CREAM_LIGHT)

    // --- ACİL DURUM KİŞİSEL TELEFON NUMARASI STATE'İ ---
    val emergencyPhoneNumber: StateFlow<String> = preferencesManager.emergencyPhoneNumber
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // --- YAKIN DEPREM BİLDİRİMLERİ AÇIK/KAPALI STATE'İ ---
    val nearbyNotificationsEnabled: StateFlow<Boolean> = preferencesManager.nearbyNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // --- SESLİ UYARI (TTS) AÇIK/KAPALI STATE'İ ---
    val voiceAlertsEnabled: StateFlow<Boolean> = preferencesManager.voiceAlertsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- BİLDİRİM FİLTRELERİ STATE'LERİ ---
    val minMagnitudeThreshold: StateFlow<Float> = preferencesManager.minMagnitudeThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4.0f)

    val maxDistanceKm: StateFlow<Float> = preferencesManager.maxDistanceKm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100.0f)

    val quietHoursEnabled: StateFlow<Boolean> = preferencesManager.quietHoursEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val quietHoursStartHour: StateFlow<Int> = preferencesManager.quietHoursStartHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)

    val quietHoursEndHour: StateFlow<Int> = preferencesManager.quietHoursEndHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 📌 Acil Durum Numarasını Yerel Hafızaya (DataStore) Kaydeder
    fun saveEmergencyPhone(phoneNumber: String) {
        viewModelScope.launch {
            preferencesManager.saveEmergencyPhoneNumber(phoneNumber)
        }
    }

    // 📌 Yakın Deprem Bildirimlerini Açar/Kapatır
    fun onNearbyNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveNearbyNotificationsEnabled(enabled)
        }
    }

    // 📌 Sesli Uyarıları (TTS) Açar/Kapatır
    fun onVoiceAlertsToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveVoiceAlertsEnabled(enabled)
        }
    }

    // 📌 Bildirim Filtreleri
    fun onMinMagnitudeChanged(value: Float) {
        viewModelScope.launch {
            preferencesManager.saveMinMagnitudeThreshold(value)
        }
    }

    fun onMaxDistanceChanged(value: Float) {
        viewModelScope.launch {
            preferencesManager.saveMaxDistanceKm(value)
        }
    }

    fun onQuietHoursToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveQuietHoursEnabled(enabled)
        }
    }

    fun onQuietHoursStartChanged(hour: Int) {
        viewModelScope.launch {
            preferencesManager.saveQuietHoursStartHour(hour)
        }
    }

    fun onQuietHoursEndChanged(hour: Int) {
        viewModelScope.launch {
            preferencesManager.saveQuietHoursEndHour(hour)
        }
    }

    // 📌 Dil Seçimi (Arayüzü Anında Yeniler)
    fun onLanguageSelected(context: Context, language: AppLanguage) {
        viewModelScope.launch {
            preferencesManager.saveSelectedLanguage(language.code)
            // Context ile birlikte çağırıyoruz ki SharedPreferences ve Activity Recreate çalışsın
            LocaleUtils.setAppLanguage(context, language.code)
        }
    }

    // 📌 Tema Seçimi (Cream Light, System Dynamic, Dark Night)
    fun onThemeSelected(themeMode: AppThemeMode) {
        viewModelScope.launch {
            preferencesManager.saveSelectedThemeMode(themeMode.name)
        }
    }

    fun onNotificationSettingsClick() {
        viewModelScope.launch {
            _eventFlow.emit(SettingsEvent.OpenNotificationSettings)
        }
    }

    fun onLocationSettingsClick() {
        viewModelScope.launch {
            _eventFlow.emit(SettingsEvent.OpenLocationSettings)
        }
    }

    fun onRateAppClick() {
        viewModelScope.launch {
            _eventFlow.emit(SettingsEvent.RequestReview)
        }
    }

    fun onShareAppClick() {
        viewModelScope.launch {
            _eventFlow.emit(SettingsEvent.ShareApp)
        }
    }

    fun onFeedbackClick() {
        viewModelScope.launch {
            _eventFlow.emit(SettingsEvent.SendEmail("destek@anlikdepremler.com"))
        }
    }

    fun onBuyCoffeeClick() {
        viewModelScope.launch {
            _eventFlow.emit(SettingsEvent.BuyCoffee("donation_small"))
        }
    }
}