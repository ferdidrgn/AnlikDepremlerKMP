package com.ferdidrgn.anlikdepremler.domain.usecase

import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode

class SaveUserPreferencesUseCase (
    private val preferencesManager: PreferencesManager
) {
    suspend fun saveTheme(themeMode: AppThemeMode) {
        preferencesManager.saveAppTheme(themeMode.name)
    }

    suspend fun saveSource(source: EarthquakeSource) {
        preferencesManager.saveSelectedSource(source.name)
    }
}