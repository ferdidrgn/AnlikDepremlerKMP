package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.ads.BannerAdView
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations.shimmer
import com.ferdidrgn.anlikdepremler.ui.components.NativeAdCard
import com.ferdidrgn.anlikdepremler.ui.components.NearbyEarthquakeAlertCard
import com.ferdidrgn.anlikdepremler.ui.components.RequestAppPermissions
import com.ferdidrgn.anlikdepremler.ui.screen.home.CreativeSourceSelector
import com.ferdidrgn.anlikdepremler.ui.screen.home.DailySummarySection
import com.ferdidrgn.anlikdepremler.ui.screen.home.ExpandableEarthquakeCard
import com.ferdidrgn.anlikdepremler.ui.screen.home.HeaderSection
import com.ferdidrgn.anlikdepremler.ui.screen.home.HeroBannerSection
import com.ferdidrgn.anlikdepremler.ui.screen.home.HistoricalArchiveBannerCard
import com.ferdidrgn.anlikdepremler.ui.screen.home.InformationTipsSliderSection
import com.ferdidrgn.anlikdepremler.ui.screen.home.LocationBasedEarthquakeCard
import com.ferdidrgn.anlikdepremler.ui.screen.home.MagnitudeDistributionChart
import com.ferdidrgn.anlikdepremler.ui.screen.home.MapPreviewCard
import com.ferdidrgn.anlikdepremler.ui.screen.home.QuickChecklistCard
import com.ferdidrgn.anlikdepremler.ui.screen.home.QuickFilters
import com.ferdidrgn.anlikdepremler.ui.screen.home.SectionTitle
import com.ferdidrgn.anlikdepremler.ui.screen.home.StatisticsSection

/**
 * Home tab: an orchestrator that lays out the sections defined under
 * [com.ferdidrgn.anlikdepremler.ui.screen.home] in order. Each section owns
 * its own rendering and local state; this file only wires data from
 * [MainViewModel] into them.
 */
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onEarthquakeClick: (Earthquake) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onHistoricalArchiveClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var rawLocationInput by remember { mutableStateOf("") }

    // 🎯 KONUM İZNİ VE SORGUSU BAŞLATMA
    RequestAppPermissions(
        onPermissionsGranted = {
            viewModel.fetchUserLocationAndSearch()
        }
    )

    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let {
            if (it.cityName.isNotEmpty() && rawLocationInput.isEmpty()) {
                rawLocationInput = it.cityName
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // 🎯 YAKIN DEPREM UYARISI KARTI
        if (uiState.nearbyAlertEarthquake != null) {
            NearbyEarthquakeAlertCard(
                earthquake = uiState.nearbyAlertEarthquake!!,
                emergencyPhone = uiState.emergencyPhoneNumber,
                onSafeClicked = { viewModel.dismissNearbyAlert() },
                onNeedHelpClicked = { viewModel.dismissNearbyAlert() }
            )
        }

        // 1. Üst Başlık & Canlı Rozet
        AppAnimations.StaggeredEntrance(index = 0) { HeaderSection() }

        // 2. Kaynak Seçici
        AppAnimations.StaggeredEntrance(index = 1) {
            CreativeSourceSelector(
                selectedSource = uiState.selectedSource,
                onSourceSelected = { viewModel.onSourceChanged(it) }
            )
        }

        BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary
            )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Hero Banner
        AppAnimations.StaggeredEntrance(index = 2) { HeroBannerSection() }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Günün Özeti Kartları
        AppAnimations.StaggeredEntrance(index = 3) { DailySummarySection(statistics = uiState.statistics) }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. İSTATİSTİKLER
        AppAnimations.StaggeredEntrance(index = 4) { StatisticsSection(statistics = uiState.statistics) }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. KONUMA GÖRE ARAMA
        LocationBasedEarthquakeCard(
            rawInput = rawLocationInput,
            isSearching = uiState.isSearchingLocation,
            onQueryChange = {
                rawLocationInput = it
                viewModel.onLocationQueryTyped(it)
            },
            earthquakes = uiState.earthquakes,
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7. DEPREM HARİTASI
        AppAnimations.StaggeredEntrance(index = 5) { MapPreviewCard() }

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Büyüklük Dağılım Grafiği
        AppAnimations.StaggeredEntrance(index = 6) { MagnitudeDistributionChart(statistics = uiState.statistics) }

        Spacer(modifier = Modifier.height(16.dp))

        // 9. Bilgi & İpuçları Carousel
        AppAnimations.StaggeredEntrance(index = 7) { InformationTipsSliderSection() }

        Spacer(modifier = Modifier.height(16.dp))

        AppAnimations.StaggeredEntrance(index = 8) { HistoricalArchiveBannerCard(onClick = onHistoricalArchiveClick) }

        Spacer(modifier = Modifier.height(16.dp))

        // 10. Acil Durum Çantası Kontrolü
        AppAnimations.StaggeredEntrance(index = 8) {
            Column {
                SectionTitle(title = stringResource(R.string.checklist_section_title))
                QuickChecklistCard()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 11. Zaman Filtreleri
        QuickFilters(
            selectedFilter = uiState.selectedTimeFilter,
            onFilterSelected = { filter -> viewModel.onTimeFilterSelected(filter) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 12. Son Depremler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_earthquakes_title, uiState.earthquakes.size),
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onSeeAllClick) {
                Text(stringResource(R.string.see_all), color = MaterialTheme.colorScheme.primary)
            }
        }

        if (uiState.isLoading) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .shimmer()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.earthquakes.take(6).forEachIndexed { index, eq ->
                    AppAnimations.SpringEntranceContainer {
                        ExpandableEarthquakeCard(
                            earthquake = eq,
                            onClick = { onEarthquakeClick(eq) }
                        )
                    }
                    if (index == 2) NativeAdCard()
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 13. Alt Sayfa Reklam Bantı
        BannerAdView()
    }
}
