package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.resources.Res
import com.ferdidrgn.anlikdepremler.resources.chart_magnitude_title
import com.ferdidrgn.anlikdepremler.resources.filter_1h
import com.ferdidrgn.anlikdepremler.resources.filter_24h
import com.ferdidrgn.anlikdepremler.resources.filter_30d
import com.ferdidrgn.anlikdepremler.resources.filter_6h
import com.ferdidrgn.anlikdepremler.resources.filter_7d
import com.ferdidrgn.anlikdepremler.resources.header_subtitle
import com.ferdidrgn.anlikdepremler.resources.header_title
import com.ferdidrgn.anlikdepremler.resources.last_update
import com.ferdidrgn.anlikdepremler.resources.source_label
import com.ferdidrgn.anlikdepremler.resources.recent_earthquakes_title
import com.ferdidrgn.anlikdepremler.resources.search_city_or_region
import com.ferdidrgn.anlikdepremler.resources.stat_avg
import com.ferdidrgn.anlikdepremler.resources.stat_max
import com.ferdidrgn.anlikdepremler.resources.stat_month
import com.ferdidrgn.anlikdepremler.resources.stat_today
import com.ferdidrgn.anlikdepremler.resources.stat_week
import com.ferdidrgn.anlikdepremler.ui.components.OfflineBanner
import com.ferdidrgn.anlikdepremler.ui.screen.HomeUiState
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor
import com.ferdidrgn.anlikdepremler.core.util.LocationUtils
import com.ferdidrgn.anlikdepremler.domain.util.parseEarthquakeInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/** Width above which the desktop dashboard layout (sidebar + table) replaces the stacked one. */
private val DESKTOP_BREAKPOINT = 900.dp

private enum class WebEarthquakeSort { NEWEST, MAGNITUDE, DEPTH, NEAREST }

/**
 * The web target's real home screen - deliberately NOT a stretched copy of the mobile phone
 * layout (a single scrolling column of cards). Wide viewports get a fixed sidebar + a dense,
 * hoverable data table that only the table itself scrolls, the way a browser-based monitoring
 * dashboard reads; narrow (mobile browser) viewports fall back to a stacked layout instead.
 */
@Composable
fun WebDashboard(
    mainViewModel: MainViewModel,
    initialEarthquakeId: String? = null,
    onEarthquakeUrlChanged: (String?) -> Unit = {},
    onShareEarthquake: () -> Unit = {}
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val isConnected by mainViewModel.isConnected.collectAsState()
    var selectedEarthquake by remember { mutableStateOf<Earthquake?>(null) }
    LaunchedEffect(initialEarthquakeId, uiState.earthquakes) {
        if (initialEarthquakeId == null) {
            selectedEarthquake = null
        } else if (uiState.earthquakes.isNotEmpty()) {
            selectedEarthquake = uiState.earthquakes.firstOrNull { it.id == initialEarthquakeId }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        if (maxWidth >= DESKTOP_BREAKPOINT) {
            DesktopDashboard(uiState, mainViewModel, onEarthquakeClick = {
                selectedEarthquake = it
                onEarthquakeUrlChanged(it.id)
            })
        } else {
            MobileWebDashboard(uiState, mainViewModel, onEarthquakeClick = {
                selectedEarthquake = it
                onEarthquakeUrlChanged(it.id)
            })
        }
        OfflineBanner(isConnected = isConnected, showingCachedData = uiState.isShowingCachedData)
    }

    selectedEarthquake?.let { eq ->
        WebEarthquakeDetailDialog(
            earthquake = eq,
            onShare = onShareEarthquake,
            onDismiss = {
                selectedEarthquake = null
                onEarthquakeUrlChanged(null)
            }
        )
    }
}

@Composable
private fun DesktopDashboard(uiState: HomeUiState, viewModel: MainViewModel, onEarthquakeClick: (Earthquake) -> Unit) {
    var minMagnitude by remember { mutableDoubleStateOf(0.0) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(WebEarthquakeSort.NEWEST) }
    val filteredEarthquakes = remember(uiState.earthquakes, minMagnitude, searchQuery, sortOrder, uiState.userLocation) {
        val filtered = uiState.earthquakes.filter { earthquake ->
            earthquake.magnitude >= minMagnitude &&
                (searchQuery.isBlank() || earthquake.location.contains(searchQuery, ignoreCase = true) ||
                    earthquake.region.contains(searchQuery, ignoreCase = true))
        }
        when (sortOrder) {
            WebEarthquakeSort.NEWEST -> filtered.sortedByDescending { earthquake ->
                parseEarthquakeInstant(earthquake.date, earthquake.time, TimeZone.currentSystemDefault())?.toEpochMilliseconds() ?: Long.MIN_VALUE
            }
            WebEarthquakeSort.MAGNITUDE -> filtered.sortedByDescending { it.magnitude }
            WebEarthquakeSort.DEPTH -> filtered.sortedBy { it.depth }
            WebEarthquakeSort.NEAREST -> uiState.userLocation?.let { location ->
                filtered.sortedBy { earthquake ->
                    LocationUtils.calculateDistanceInKm(location.latitude, location.longitude, earthquake.latitude, earthquake.longitude)
                }
            } ?: filtered
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WebBrandHeader(stringResource(Res.string.header_title))
                WebThemeToggle(current = uiState.currentTheme, onSelected = viewModel::onThemeChanged)
            }
            Spacer(modifier = Modifier.height(16.dp))
            SeismicWaveform(
                modifier = Modifier.fillMaxWidth().height(32.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "VERİ KAYNAĞI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            WebSourceList(selected = uiState.selectedSource, onSelected = viewModel::onSourceChanged)

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                stringResource(Res.string.chart_magnitude_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            WebMagnitudeMiniChart(uiState.statistics.magnitudeDistribution)
            Spacer(modifier = Modifier.height(20.dp))
            WebActivityInsights(uiState.earthquakes)
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(28.dp)
        ) {
            Text(stringResource(Res.string.header_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(Res.string.header_title), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            DataFreshnessText(uiState)

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                WebErrorBanner(message = message, onRetry = viewModel::loadEarthquakes)
            }

            Spacer(modifier = Modifier.height(20.dp))
            WebStatsGrid(uiState)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "KEŞFET: DEPREM HARİTASI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            WebEarthquakeMapView(filteredEarthquakes, onEarthquakeClick = onEarthquakeClick)

            Spacer(modifier = Modifier.height(20.dp))
            WebTimeFilterRow(
                selected = uiState.selectedTimeFilter,
                labels = timeFilterLabels(),
                onSelected = viewModel::onTimeFilterSelected
            )
            Spacer(modifier = Modifier.height(10.dp))
            WebMagnitudeFilterRow(selected = minMagnitude, onSelected = { minMagnitude = it })

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.search_city_or_region)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            WebEarthquakeSortRow(
                selected = sortOrder,
                hasLocation = uiState.userLocation != null,
                onSelected = { sortOrder = it },
                onRequestLocation = viewModel::fetchUserLocationAndSearch
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(Res.string.recent_earthquakes_title, filteredEarthquakes.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(440.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    WebEarthquakeTableHeader()
                    EarthquakeTableBody(uiState, filteredEarthquakes, onEarthquakeClick)
                }
            }
        }
    }
}

@Composable
private fun MobileWebDashboard(uiState: HomeUiState, viewModel: MainViewModel, onEarthquakeClick: (Earthquake) -> Unit) {
    var minMagnitude by remember { mutableDoubleStateOf(0.0) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(WebEarthquakeSort.NEWEST) }
    val filteredEarthquakes = remember(uiState.earthquakes, minMagnitude, searchQuery, sortOrder, uiState.userLocation) {
        val filtered = uiState.earthquakes.filter { earthquake ->
            earthquake.magnitude >= minMagnitude &&
                (searchQuery.isBlank() || earthquake.location.contains(searchQuery, ignoreCase = true) ||
                    earthquake.region.contains(searchQuery, ignoreCase = true))
        }
        when (sortOrder) {
            WebEarthquakeSort.NEWEST -> filtered.sortedByDescending { earthquake ->
                parseEarthquakeInstant(earthquake.date, earthquake.time, TimeZone.currentSystemDefault())?.toEpochMilliseconds() ?: Long.MIN_VALUE
            }
            WebEarthquakeSort.MAGNITUDE -> filtered.sortedByDescending { it.magnitude }
            WebEarthquakeSort.DEPTH -> filtered.sortedBy { it.depth }
            WebEarthquakeSort.NEAREST -> uiState.userLocation?.let { location ->
                filtered.sortedBy { earthquake ->
                    LocationUtils.calculateDistanceInKm(location.latitude, location.longitude, earthquake.latitude, earthquake.longitude)
                }
            } ?: filtered
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WebBrandHeader(stringResource(Res.string.header_title))
                    WebThemeToggle(current = uiState.currentTheme, onSelected = viewModel::onThemeChanged)
                }
                DataFreshnessText(uiState)
                Spacer(modifier = Modifier.height(12.dp))

                uiState.errorMessage?.let { message ->
                    WebErrorBanner(message = message, onRetry = viewModel::loadEarthquakes)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                SeismicWaveform(
                    modifier = Modifier.fillMaxWidth().height(28.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                WebSourceList(selected = uiState.selectedSource, onSelected = viewModel::onSourceChanged)
                Spacer(modifier = Modifier.height(20.dp))
                WebStatsGrid(uiState)
                Spacer(modifier = Modifier.height(20.dp))
                WebActivityInsights(uiState.earthquakes)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "KEŞFET: DEPREM HARİTASI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                WebEarthquakeMapView(filteredEarthquakes, onEarthquakeClick = onEarthquakeClick)
                Spacer(modifier = Modifier.height(16.dp))

                WebTimeFilterRow(
                    selected = uiState.selectedTimeFilter,
                    labels = timeFilterLabels(),
                    onSelected = viewModel::onTimeFilterSelected
                )
                Spacer(modifier = Modifier.height(10.dp))
                WebMagnitudeFilterRow(selected = minMagnitude, onSelected = { minMagnitude = it })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.search_city_or_region)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                WebEarthquakeSortRow(
                    selected = sortOrder,
                    hasLocation = uiState.userLocation != null,
                    onSelected = { sortOrder = it },
                    onRequestLocation = viewModel::fetchUserLocationAndSearch
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(Res.string.recent_earthquakes_title, filteredEarthquakes.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when {
            uiState.isLoading && uiState.earthquakes.isEmpty() -> item { LoadingState() }
            filteredEarthquakes.isEmpty() -> item { EmptyState() }
            else -> itemsIndexed(
                items = filteredEarthquakes,
                key = { _, earthquake -> earthquake.id }
            ) { index, earthquake ->
                StaggeredEntrance(index = index) {
                    WebEarthquakeCardCompact(
                        earthquake = earthquake,
                        onClick = { onEarthquakeClick(earthquake) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WebEarthquakeSortRow(
    selected: WebEarthquakeSort,
    hasLocation: Boolean,
    onSelected: (WebEarthquakeSort) -> Unit,
    onRequestLocation: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            WebEarthquakeSort.NEWEST to "En yeni",
            WebEarthquakeSort.MAGNITUDE to "En büyük",
            WebEarthquakeSort.DEPTH to "En derin",
            WebEarthquakeSort.NEAREST to if (hasLocation) "Yakınımda" else "Konumuma göre"
        ).forEach { (order, label) ->
            val isSelected = selected == order
            androidx.compose.material3.Surface(
                onClick = {
                    onSelected(order)
                    if (order == WebEarthquakeSort.NEAREST && !hasLocation) onRequestLocation()
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun DataFreshnessText(uiState: HomeUiState) {
    val updatedAt = uiState.lastSuccessfulUpdateMillis ?: return
    val source = uiState.lastSuccessfulSourceName ?: uiState.selectedSource.displayName
    val localTime = Instant.fromEpochMilliseconds(updatedAt)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val timeText = "${localTime.date} ${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}"
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.source_label, source),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(Res.string.last_update, timeText),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WebStatsGrid(uiState: HomeUiState) {
    val stats = uiState.statistics
    val tiles = listOf(
        Triple(stringResource(Res.string.stat_today), stats.totalToday.toString(), MaterialTheme.colorScheme.primary),
        Triple(stringResource(Res.string.stat_week), stats.totalWeek.toString(), MaterialTheme.colorScheme.secondary),
        Triple(stringResource(Res.string.stat_month), stats.totalMonth.toString(), MaterialTheme.colorScheme.tertiary),
        Triple(stringResource(Res.string.stat_avg), formatMagnitude(stats.avgMagnitude), magnitudeHeatColor(stats.avgMagnitude)),
        Triple(stringResource(Res.string.stat_max), formatMagnitude(stats.maxMagnitude), magnitudeHeatColor(stats.maxMagnitude))
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 640.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tiles.chunked(2).forEachIndexed { rowIndex, rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowTiles.forEachIndexed { columnIndex, (label, value, accent) ->
                            StaggeredEntrance(
                                index = rowIndex * 2 + columnIndex,
                                modifier = Modifier.weight(1f)
                            ) {
                                WebStatTile(label, value, accent, Modifier.fillMaxWidth())
                            }
                        }
                        if (rowTiles.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                tiles.forEachIndexed { index, (label, value, accent) ->
                    StaggeredEntrance(index = index, modifier = Modifier.weight(1f)) {
                        WebStatTile(label, value, accent, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun EarthquakeTableBody(
    uiState: HomeUiState,
    filteredEarthquakes: List<Earthquake>,
    onEarthquakeClick: (Earthquake) -> Unit
) {
    when {
        uiState.isLoading && uiState.earthquakes.isEmpty() -> LoadingState()
        filteredEarthquakes.isEmpty() -> EmptyState()
        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(filteredEarthquakes, key = { _, eq -> eq.id }) { index, eq ->
                StaggeredEntrance(index = index) {
                    Column {
                        WebEarthquakeRow(earthquake = eq, onClick = { onEarthquakeClick(eq) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Text("Gösterilecek deprem verisi yok.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun timeFilterLabels() = mapOf(
    "1s" to stringResource(Res.string.filter_1h),
    "6s" to stringResource(Res.string.filter_6h),
    "24s" to stringResource(Res.string.filter_24h),
    "7g" to stringResource(Res.string.filter_7d),
    "30g" to stringResource(Res.string.filter_30d)
)

private fun formatMagnitude(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10.0
    return rounded.toString()
}
