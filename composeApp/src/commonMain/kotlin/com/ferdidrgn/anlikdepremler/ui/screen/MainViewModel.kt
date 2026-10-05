package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferdi.deprem.model.Earthquake
import com.ferdi.deprem.model.EarthquakeStatistics
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.notification.NearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import com.ferdidrgn.anlikdepremler.core.util.LocationUtils
import com.ferdidrgn.anlikdepremler.core.util.UserLocationResult
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.domain.usecase.*
import com.ferdidrgn.anlikdepremler.domain.util.filterByTimeSpan
import com.ferdidrgn.anlikdepremler.ui.theme.AppThemeMode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class HomeUiState(
    val isLoading: Boolean = false,
    /** A background refresh is in flight while the list already has data to show - unlike
     *  [isLoading], the UI must never swap the visible list out for a loading skeleton for this. */
    val isRefreshing: Boolean = false,
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

/**
 * Shared across Android, iOS and web: none of its dependencies are Android-specific
 * (NetworkMonitor/LocationTracker are expect/actual, PreferencesManager wraps a
 * cross-platform DataStore, ViewModel/viewModelScope come from the KMP lifecycle-viewmodel
 * artifact). Each platform still injects it its own way - Android via Koin's
 * androidx-compose `viewModel {}` DSL (tied to the Activity's ViewModelStore), web/iOS via a
 * plain Koin `single` since there is no platform ViewModelStore to tie it to there.
 */
class MainViewModel(
    private val getEarthquakesUseCase: GetEarthquakesUseCase,
    private val calculateStatisticsUseCase: CalculateStatisticsUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: NetworkMonitor,
    private val locationTracker: LocationTracker,
    private val nearbyEarthquakeNotifier: NearbyEarthquakeNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _locationQueryState = MutableStateFlow("")

    /** The last earthquake id a system notification was already posted for - avoids re-firing
     *  one on every auto-refresh poll while it's still the nearest qualifying quake. */
    private var lastNotifiedEarthquakeId: String? = null

    /** Whether the app is currently visible to the user - set by the platform layer from the
     *  Activity/page lifecycle. Auto-refresh skips its network call while this is false, since
     *  the earthquake APIs it polls are free third-party services we don't operate, not ours
     *  to hammer on a timer while nobody's even looking at the screen. */
    private var isAppForeground = true

    fun setAppForeground(foreground: Boolean) {
        val wasBackground = !isAppForeground
        isAppForeground = foreground
        if (foreground && wasBackground) {
            loadEarthquakes()
        }
    }

    val isOnboardingCompleted = preferencesManager.isOnboardingCompleted
        .catch { emit(false) }
        .stateIn(
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
        startAutoRefresh()
    }

    /** Nothing was re-fetching earthquake data once the first load finished - this keeps the
     *  list "live" instead of only ever updating on a source change or search. Every 3 minutes
     *  rather than every minute, and skipped entirely while backgrounded (see
     *  [isAppForeground]/[setAppForeground]) - these are free public earthquake APIs we don't
     *  run ourselves, and polling them once a minute from every device around the clock is the
     *  kind of load that gets an app's IP/key blocked. */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL)
                if (isAppForeground) {
                    loadEarthquakes()
                }
            }
        }
    }

    private fun observeEmergencyPhone() {
        viewModelScope.launch {
            preferencesManager.emergencyPhoneNumber
                .catch { /* no persisted phone number available on this platform yet */ }
                .collect { phone ->
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

    private suspend fun checkNearbyEarthquakes(userLoc: UserLocationResult) {
        val minMagnitude = preferencesManager.minMagnitudeThreshold.first().toDouble()
        val maxDistanceKm = preferencesManager.maxDistanceKm.first().toDouble()
        val savedLocations = preferencesManager.savedLocations.first()

        // Checks the live GPS fix plus every saved location (home/work/family) - an earthquake
        // counts as "nearby" if it's close to ANY of them, using whichever is closest.
        val checkPoints = buildList {
            add(userLoc.latitude to userLoc.longitude)
            savedLocations.forEach { add(it.latitude to it.longitude) }
        }

        val nearest = _uiState.value.rawEarthquakes
            .asSequence()
            .filter { it.magnitude >= minMagnitude }
            .mapNotNull { eq ->
                val closestDistanceKm = checkPoints.minOf { (lat, lng) ->
                    LocationUtils.calculateDistanceInKm(
                        userLat = lat,
                        userLng = lng,
                        eqLat = eq.latitude,
                        eqLng = eq.longitude
                    )
                }
                if (closestDistanceKm <= maxDistanceKm) eq to closestDistanceKm else null
            }
            .minByOrNull { (_, distanceKm) -> distanceKm }

        _uiState.update { it.copy(nearbyAlertEarthquake = nearest?.first) }

        val (earthquake, distanceKm) = nearest ?: return
        if (earthquake.id == lastNotifiedEarthquakeId) return

        if (preferencesManager.nearbyNotificationsEnabled.first() && !isWithinQuietHours()) {
            nearbyEarthquakeNotifier.notifyNearbyEarthquake(earthquake, distanceKm)
            lastNotifiedEarthquakeId = earthquake.id
        }
    }

    /** Nearby-earthquake notifications are suppressed inside this local-time window - the
     *  in-app banner (nearbyAlertEarthquake) still updates either way, only the system
     *  notification/TTS announcement is held back. */
    private suspend fun isWithinQuietHours(): Boolean {
        if (!preferencesManager.quietHoursEnabled.first()) return false

        val startHour = preferencesManager.quietHoursStartHour.first()
        val endHour = preferencesManager.quietHoursEndHour.first()
        val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour

        return if (startHour <= endHour) {
            currentHour in startHour until endHour
        } else {
            // Window wraps past midnight, e.g. 22 -> 7
            currentHour >= startHour || currentHour < endHour
        }
    }

    fun dismissNearbyAlert() {
        _uiState.update { it.copy(nearbyAlertEarthquake = null) }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            getUserPreferencesUseCase()
                .catch {
                    // Preference storage isn't available (e.g. an unsupported browser) -
                    // fall back to HomeUiState()'s defaults and still load earthquake data.
                    loadEarthquakes()
                }
                .collect { prefs ->
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
            // A refresh with data already on screen must never flip isLoading - that's the flag
            // HomeScreen/EarthquakeListScreen use to swap the real list out for a loading
            // skeleton, which was yanking whatever the user was scrolled to back to a 4-row
            // skeleton on every background poll.
            val hasExistingData = _uiState.value.rawEarthquakes.isNotEmpty()
            _uiState.update {
                if (hasExistingData) it.copy(isRefreshing = true) else it.copy(isLoading = true)
            }

            val activeQuery =
                _uiState.value.locationSearchQuery.ifEmpty { _uiState.value.searchQuery }

            getEarthquakesUseCase(
                source = _uiState.value.selectedSource,
                query = activeQuery
            ).catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = e.localizedMessage())
                }
            }.collect { list ->
                val stats = calculateStatisticsUseCase(list)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
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

    companion object {
        private val AUTO_REFRESH_INTERVAL = 3.minutes
    }
}

/** java.lang.Throwable.localizedMessage isn't available outside the JVM; this is. */
private fun Throwable.localizedMessage(): String? = message
