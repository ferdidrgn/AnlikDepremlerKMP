package com.ferdidrgn.anlikdepremler.domain.usecase

import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class UserPreferences(
    val themeMode: AppThemeMode,
    val selectedSource: EarthquakeSource
)

class GetUserPreferencesUseCase (
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(): Flow<UserPreferences> {
        return combine(
            preferencesManager.appTheme,
            preferencesManager.selectedSource
        ) { themeName, sourceName ->
            val themeMode = try {
                AppThemeMode.valueOf(themeName)
            } catch (e: Exception) {
                AppThemeMode.CREAM_LIGHT
            }

            val source = try {
                EarthquakeSource.valueOf(sourceName)
            } catch (e: Exception) {
                EarthquakeSource.KANDILLI
            }

            UserPreferences(themeMode, source)
        }
    }
}