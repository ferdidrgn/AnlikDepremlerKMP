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

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 📌 Acil Durum Numarasını Yerel Hafızaya (DataStore) Kaydeder
    fun saveEmergencyPhone(phoneNumber: String) {
        viewModelScope.launch {
            preferencesManager.saveEmergencyPhoneNumber(phoneNumber)
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