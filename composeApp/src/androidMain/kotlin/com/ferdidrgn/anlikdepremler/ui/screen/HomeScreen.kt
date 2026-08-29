package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ferdi.deprem.model.Earthquake
import com.ferdi.deprem.model.EarthquakeStatistics
import com.ferdi.deprem.model.InfoCardItem
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.ads.BannerAdView
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations.shimmer
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.ui.components.NativeAdCard
import com.ferdidrgn.anlikdepremler.ui.components.NearbyEarthquakeAlertCard
import com.ferdidrgn.anlikdepremler.ui.components.RequestAppPermissions

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
        HeaderSection()

        // 2. Kaynak Seçici
        CreativeSourceSelector(
            selectedSource = uiState.selectedSource,
            onSourceSelected = { viewModel.onSourceChanged(it) }
        )

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
        HeroBannerSection()

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Günün Özeti Kartları
        DailySummarySection(statistics = uiState.statistics)

        Spacer(modifier = Modifier.height(16.dp))

        // 5. İSTATİSTİKLER
        StatisticsSection(statistics = uiState.statistics)

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
        MapPreviewCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Büyüklük Dağılım Grafiği
        MagnitudeDistributionChart(statistics = uiState.statistics)

        Spacer(modifier = Modifier.height(16.dp))

        // 9. Bilgi & İpuçları Carousel
        InformationTipsSliderSection()

        Spacer(modifier = Modifier.height(16.dp))

        // HomeScreen.kt içindeki Column düzeninde istediğin yere yapıştır:
        HistoricalArchiveBannerCard(onClick = onHistoricalArchiveClick)

        Spacer(modifier = Modifier.height(16.dp))

        // 10. Acil Durum Çantası Kontrolü
        SectionTitle(title = stringResource(R.string.checklist_section_title))
        QuickChecklistCard()

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

@Composable
fun CreativeSourceSelector(
    selectedSource: EarthquakeSource,
    onSourceSelected: (EarthquakeSource) -> Unit
) {
    val sources = listOf(
        EarthquakeSource.KANDILLI to stringResource(R.string.source_kandilli),
        EarthquakeSource.AFAD to stringResource(R.string.source_afad),
        EarthquakeSource.TURKEY_ALL to stringResource(R.string.source_turkey_all),
        EarthquakeSource.USGS to stringResource(R.string.source_usgs),
        EarthquakeSource.WORLD_IGP to stringResource(R.string.source_world_igp),
        EarthquakeSource.EMSC to stringResource(R.string.source_emsc)
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(R.string.data_source_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sources) { (source, label) ->
                val isSelected = selectedSource == source
                Surface(
                    onClick = { onSourceSelected(source) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = if (!isSelected) BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ) else null
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LocationBasedEarthquakeCard(
    rawInput: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    earthquakes: List<Earthquake>,
    onSeeAllClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.location_based_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onSeeAllClick() }
                ) {
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.location_based_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rawInput,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.location_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (earthquakes.isEmpty()) {
                Text(
                    text = stringResource(R.string.location_no_result),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(earthquakes) { eq ->
                        Card(
                            modifier = Modifier.width(180.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = eq.location,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${eq.magnitude} Mw • ${eq.depth} km",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                stringResource(R.string.header_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.header_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    stringResource(R.string.live_data),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HeroBannerSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = "https://picsum.photos/800/400?random=20",
            contentDescription = stringResource(R.string.hero_guide_title),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    stringResource(R.string.hero_guide_tag),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                stringResource(R.string.hero_guide_title),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(R.string.hero_guide_desc),
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DailySummarySection(statistics: EarthquakeStatistics) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(R.string.daily_summary_title),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.daily_summary_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${statistics.totalToday}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.total_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format("%.1f", statistics.maxMagnitude),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.highest),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MapPreviewCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.map_preview_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.map_preview_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InformationTipsSliderSection() {
    val localInfoCards = remember {
        listOf(
            InfoCardItem(
                1,
                "Deprem Çantası Hazırlığı",
                "Hayatta kalma çantanızda bulunması gereken temel malzemeler.",
                "Hazırlık",
                "https://picsum.photos/600/400?random=10"
            ),
            InfoCardItem(
                2,
                "Çök - Kapan - Tutun",
                "Deprem anında doğru pozisyon hayat kurtarır.",
                "Güvenlik",
                "https://picsum.photos/600/400?random=11"
            ),
            InfoCardItem(
                3,
                "Bina Sağlamlığı Testi",
                "Oturduğunuz binanın risk analizini nasıl yaptırırsınız?",
                "Bilinç",
                "https://picsum.photos/600/400?random=12"
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.info_tips_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.info_tips_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(localInfoCards) { item ->
                    Card(
                        modifier = Modifier
                            .width(230.dp)
                            .height(150.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.description,
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
fun StatisticsSection(statistics: EarthquakeStatistics) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.detailed_stats_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_today),
                value = statistics.totalToday.toString(),
                subtitle = stringResource(R.string.unit_earthquake),
                color = MaterialTheme.colorScheme.primary,
                icon = "🔴",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_week),
                value = statistics.totalWeek.toString(),
                subtitle = stringResource(R.string.unit_earthquake),
                color = MaterialTheme.colorScheme.secondary,
                icon = "📅",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_month),
                value = statistics.totalMonth.toString(),
                subtitle = stringResource(R.string.unit_earthquake),
                color = MaterialTheme.colorScheme.tertiary,
                icon = "📆",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = stringResource(R.string.stat_avg),
                value = String.format("%.1f", statistics.avgMagnitude),
                subtitle = "Mw",
                color = MaterialTheme.colorScheme.primary,
                icon = "📊",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_max),
                value = String.format("%.1f", statistics.maxMagnitude),
                subtitle = "Mw",
                color = MaterialTheme.colorScheme.error,
                icon = "⚠️",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stat_active),
                value = statistics.mostActiveRegion,
                subtitle = stringResource(R.string.unit_region),
                color = MaterialTheme.colorScheme.secondary,
                icon = "📍",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Column {
                Text(
                    text = value,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$title • $subtitle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun MagnitudeDistributionChart(statistics: EarthquakeStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.chart_magnitude_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.chart_last_24h),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val data = statistics.magnitudeDistribution
            val maxValue = data.values.maxOrNull()?.toFloat() ?: 1f

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                data.forEach { (range, count) ->
                    val percentage = (count.toFloat() / maxValue) * 100f
                    val color = when (range) {
                        "1-2" -> MaterialTheme.colorScheme.primary
                        "2-3" -> MaterialTheme.colorScheme.secondary
                        "3-4" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            range,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp)
                        )
                        AnimatedBar(
                            percentage = percentage,
                            color = color,
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                        )
                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedBar(percentage: Float, color: Color, modifier: Modifier = Modifier) {
    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(percentage) {
        animationProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = percentage,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animationProgress = value
        }
    }

    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = (animationProgress / 100f).coerceIn(0f, 1f))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.7f),
                            color
                        )
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
fun ExpandableEarthquakeCard(
    earthquake: Earthquake,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val magnitudeColor = when {
        earthquake.magnitude < 2.0 -> MaterialTheme.colorScheme.primary
        earthquake.magnitude < 3.5 -> MaterialTheme.colorScheme.secondary
        earthquake.magnitude < 5.0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(magnitudeColor.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MagnitudeIndicator(magnitude = earthquake.magnitude, color = magnitudeColor)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = earthquake.location,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${earthquake.region} • ${earthquake.date} ${earthquake.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.1f", earthquake.depth)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (earthquake.isSignificant) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.important_warning),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailItem(
                            label = stringResource(R.string.label_magnitude),
                            value = String.format("%.1f", earthquake.magnitude),
                            unit = "Mw"
                        )
                        DetailItem(
                            label = stringResource(R.string.label_depth),
                            value = String.format("%.1f", earthquake.depth),
                            unit = "km"
                        )
                        DetailItem(
                            label = stringResource(R.string.label_intensity),
                            value = earthquake.intensity,
                            unit = ""
                        )
                    }
                }
            }

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = if (isExpanded) stringResource(R.string.show_less) else stringResource(R.string.show_more),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MagnitudeIndicator(magnitude: Double, color: Color) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        if (magnitude > 4.0) {
            val pulseScale by AppAnimations.rememberPulseScale(targetScale = 1.25f)

            Box(
                modifier = Modifier
                    .size(52.dp * pulseScale)
                    .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(50))
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", magnitude),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(text = "Mw", color = color.copy(alpha = 0.7f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
        Text(
            text = if (unit.isNotEmpty()) "$label ($unit)" else label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
fun QuickChecklistCard() {
    var checkedState by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.checklist_item_food_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    stringResource(R.string.checklist_item_food_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            Checkbox(
                checked = checkedState,
                onCheckedChange = { checkedState = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun QuickFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("1s", "6s", "24s", "7g", "30g")
    val filterLabels = mapOf(
        "1s" to stringResource(R.string.filter_1h),
        "6s" to stringResource(R.string.filter_6h),
        "24s" to stringResource(R.string.filter_24h),
        "7g" to stringResource(R.string.filter_7d),
        "30g" to stringResource(R.string.filter_30d)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filterLabels[filter] ?: filter, fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun HistoricalArchiveBannerCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📜", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.historical_archive_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.historical_archive_subtitle),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}