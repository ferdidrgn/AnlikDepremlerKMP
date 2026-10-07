package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ferdidrgn.anlikdepremler.resources.recent_earthquakes_title
import com.ferdidrgn.anlikdepremler.resources.stat_avg
import com.ferdidrgn.anlikdepremler.resources.stat_max
import com.ferdidrgn.anlikdepremler.resources.stat_month
import com.ferdidrgn.anlikdepremler.resources.stat_today
import com.ferdidrgn.anlikdepremler.resources.stat_week
import com.ferdidrgn.anlikdepremler.ui.screen.HomeUiState
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor
import org.jetbrains.compose.resources.stringResource

/** Width above which the desktop dashboard layout (sidebar + table) replaces the stacked one. */
private val DESKTOP_BREAKPOINT = 900.dp

/**
 * The web target's real home screen - deliberately NOT a stretched copy of the mobile phone
 * layout (a single scrolling column of cards). Wide viewports get a fixed sidebar + a dense,
 * hoverable data table that only the table itself scrolls, the way a browser-based monitoring
 * dashboard reads; narrow (mobile browser) viewports fall back to a stacked layout instead.
 */
@Composable
fun WebDashboard(mainViewModel: MainViewModel) {
    val uiState by mainViewModel.uiState.collectAsState()
    var selectedEarthquake by remember { mutableStateOf<Earthquake?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        if (maxWidth >= DESKTOP_BREAKPOINT) {
            DesktopDashboard(uiState, mainViewModel, onEarthquakeClick = { selectedEarthquake = it })
        } else {
            MobileWebDashboard(uiState, mainViewModel, onEarthquakeClick = { selectedEarthquake = it })
        }
    }

    selectedEarthquake?.let { eq ->
        WebEarthquakeDetailDialog(earthquake = eq, onDismiss = { selectedEarthquake = null })
    }
}

@Composable
private fun DesktopDashboard(uiState: HomeUiState, viewModel: MainViewModel, onEarthquakeClick: (Earthquake) -> Unit) {
    var minMagnitude by remember { mutableDoubleStateOf(0.0) }
    val filteredEarthquakes = remember(uiState.earthquakes, minMagnitude) {
        uiState.earthquakes.filter { it.magnitude >= minMagnitude }
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            WebSourceList(selected = uiState.selectedSource, onSelected = viewModel::onSourceChanged)

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                stringResource(Res.string.chart_magnitude_title),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            WebMagnitudeMiniChart(uiState.statistics.magnitudeDistribution)
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(28.dp)) {
            Text(stringResource(Res.string.header_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(Res.string.header_title), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                WebErrorBanner(message = message, onRetry = viewModel::loadEarthquakes)
            }

            Spacer(modifier = Modifier.height(20.dp))
            WebStatsGrid(uiState)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "KEŞFET: DEPREM HARİTASI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            EarthquakeMapCanvas(filteredEarthquakes)

            Spacer(modifier = Modifier.height(20.dp))
            WebTimeFilterRow(
                selected = uiState.selectedTimeFilter,
                labels = timeFilterLabels(),
                onSelected = viewModel::onTimeFilterSelected
            )
            Spacer(modifier = Modifier.height(10.dp))
            WebMagnitudeFilterRow(selected = minMagnitude, onSelected = { minMagnitude = it })

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(Res.string.recent_earthquakes_title, filteredEarthquakes.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
    val filteredEarthquakes = remember(uiState.earthquakes, minMagnitude) {
        uiState.earthquakes.filter { it.magnitude >= minMagnitude }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Text(
            "KEŞFET: DEPREM HARİTASI",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        EarthquakeMapCanvas(filteredEarthquakes)
        Spacer(modifier = Modifier.height(16.dp))

        WebTimeFilterRow(
            selected = uiState.selectedTimeFilter,
            labels = timeFilterLabels(),
            onSelected = viewModel::onTimeFilterSelected
        )
        Spacer(modifier = Modifier.height(10.dp))
        WebMagnitudeFilterRow(selected = minMagnitude, onSelected = { minMagnitude = it })
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            stringResource(Res.string.recent_earthquakes_title, filteredEarthquakes.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading && uiState.earthquakes.isEmpty()) {
            LoadingState()
        } else if (filteredEarthquakes.isEmpty()) {
            EmptyState()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredEarthquakes.forEachIndexed { index, eq ->
                    StaggeredEntrance(index = index) {
                        WebEarthquakeCardCompact(earthquake = eq, onClick = { onEarthquakeClick(eq) })
                    }
                }
            }
        }
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.forEachIndexed { index, (label, value, accent) ->
            StaggeredEntrance(index = index, modifier = Modifier.weight(1f)) {
                WebStatTile(label, value, accent, Modifier.fillMaxWidth())
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
