package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferdi.deprem.model.Earthquake
import com.ferdi.deprem.model.EarthquakeStatistics
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import com.ferdidrgn.anlikdepremler.core.util.LocationUtils
import com.ferdidrgn.anlikdepremler.core.util.UserLocationResult
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.domain.usecase.*
import com.ferdidrgn.anlikdepremler.domain.util.filterByTimeSpan
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class HomeUiState(
    val isLoading: Boolean = false,
    val earthquakes: List<Earthquake> = emptyList(),
    val rawEarthquakes: List<Earthquake> = emptyList(),
    val statistics: EarthquakeStatistics = EarthquakeStatistics(0, 0, 0, 0.0, 0.0, "-", emptyMap()),
    val selectedSource: EarthquakeSource = EarthquakeSource.KANDILLI,
    val currentTheme: AppThemeMode = AppThemeMode.CREAM_LIGHT,
    val selectedTimeFilter: String = "24s",
    val searchQuery: String = "",
    val locationSearchQuery: String = "",
    val isSearchingLocation: Boolean = false,
    val errorMessage: String? = null,
    val userLocation: UserLocationResult? = null,
    val nearbyAlertEarthquake: Earthquake? = null,
    val emergencyPhoneNumber: String = ""
)

class MainViewModel(
    private val getEarthquakesUseCase: GetEarthquakesUseCase,
    private val calculateStatisticsUseCase: CalculateStatisticsUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: NetworkMonitor,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _locationQueryState = MutableStateFlow("")

    val isOnboardingCompleted = preferencesManager.isOnboardingCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        observeUserPreferences()
        observeEmergencyPhone()
        setupDebouncedSearch()
    }

    private fun observeEmergencyPhone() {
        viewModelScope.launch {
            preferencesManager.emergencyPhoneNumber.collect { phone ->
                _uiState.update { it.copy(emergencyPhoneNumber = phone) }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.saveOnboardingCompleted(true)
        }
    }

    // 📍 KONUM İZNİ VERİLDİĞİNDE ÇAĞRILAN MERKEZİ METOT
    fun fetchUserLocationAndSearch() {
        viewModelScope.launch {
            val locationResult = locationTracker.getCurrentLocation()
            if (locationResult != null) {
                _uiState.update {
                    it.copy(
                        userLocation = locationResult,
                        locationSearchQuery = locationResult.cityName
                    )
                }
                // Otomatik Şehir Araması Yapılıyor
                if (locationResult.cityName.isNotEmpty()) {
                    onLocationQueryTyped(locationResult.cityName)
                }
                // Yakın Deprem Analizi Yapılıyor
                checkNearbyEarthquakes(locationResult)
            }
        }
    }

    private fun checkNearbyEarthquakes(userLoc: UserLocationResult) {
        val criticalEarthquake = _uiState.value.rawEarthquakes.firstOrNull { eq ->
            eq.magnitude >= 4.0 && LocationUtils.calculateDistanceInKm(
                userLat = userLoc.latitude,
                userLng = userLoc.longitude,
                eqLat = eq.latitude,
                eqLng = eq.longitude
            ) <= 100.0
        }
        _uiState.update { it.copy(nearbyAlertEarthquake = criticalEarthquake) }
    }

    fun dismissNearbyAlert() {
        _uiState.update { it.copy(nearbyAlertEarthquake = null) }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { prefs ->
                _uiState.update {
                    it.copy(
                        selectedSource = prefs.selectedSource,
                        currentTheme = prefs.themeMode
                    )
                }
                loadEarthquakes()
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        viewModelScope.launch {
            _locationQueryState
                .debounce(500L.milliseconds)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchQuery = query
                        )
                    }
                    loadEarthquakes()
                }
        }
    }

    fun loadEarthquakes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val activeQuery =
                _uiState.value.locationSearchQuery.ifEmpty { _uiState.value.searchQuery }

            getEarthquakesUseCase(
                source = _uiState.value.selectedSource,
                query = activeQuery
            ).catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }.collect { list ->
                val stats = calculateStatisticsUseCase(list)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rawEarthquakes = list,
                        earthquakes = list.filterByTimeSpan(it.selectedTimeFilter),
                        statistics = stats,
                        errorMessage = null
                    )
                }
                _uiState.value.userLocation?.let { checkNearbyEarthquakes(it) }
            }
        }
    }

    fun onTimeFilterSelected(filter: String) {
        _uiState.update { current ->
            current.copy(
                selectedTimeFilter = filter,
                earthquakes = current.rawEarthquakes.filterByTimeSpan(filter)
            )
        }
    }

    fun onSourceChanged(newSource: EarthquakeSource) {
        viewModelScope.launch {
            saveUserPreferencesUseCase.saveSource(newSource)
        }
    }

    fun onThemeChanged(newTheme: AppThemeMode) {
        viewModelScope.launch {
            saveUserPreferencesUseCase.saveTheme(newTheme)
        }
    }

    fun onLocationQueryTyped(newText: String) {
        _uiState.update { it.copy(isSearchingLocation = true) }
        _locationQueryState.value = newText
    }
}