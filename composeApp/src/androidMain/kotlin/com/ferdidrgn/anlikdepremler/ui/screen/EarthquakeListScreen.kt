package com.ferdidrgn.anlikdepremler.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations.shimmer
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.ui.components.NativeAdCard
import com.ferdidrgn.anlikdepremler.ui.util.shouldShowAdAtIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeListScreen(
    viewModel: MainViewModel,
    onEarthquakeClick: (Earthquake) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMagFilter by remember { mutableDoubleStateOf(0.0) }

    // 🎯 Multidil Desteği İçin Strings.xml Okumaları (Scope Dışında Güvenli Okuma)
    val text1h = stringResource(R.string.filter_1h)
    val text6h = stringResource(R.string.filter_6h)
    val text24h = stringResource(R.string.filter_24h)
    val text7d = stringResource(R.string.filter_7d)
    val text30d = stringResource(R.string.filter_30d)
    val textAll = stringResource(R.string.filter_all)

    val timeFilters = remember(text1h, text6h, text24h, text7d, text30d) {
        listOf(
            "1s" to text1h,
            "6s" to text6h,
            "24s" to text24h,
            "7g" to text7d,
            "30g" to text30d
        )
    }

    val magFilters = remember(textAll) {
        listOf(
            0.0 to textAll,
            2.0 to "2.0+",
            3.0 to "3.0+",
            4.0 to "4.0+",
            5.0 to "5.0+"
        )
    }

    val filteredList = remember(uiState.earthquakes, searchQuery, selectedMagFilter) {
        uiState.earthquakes.filter { eq ->
            val matchesSearch = eq.location.contains(searchQuery, ignoreCase = true) ||
                    eq.region.contains(searchQuery, ignoreCase = true)
            val matchesMag = eq.magnitude >= selectedMagFilter
            matchesSearch && matchesMag
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        // 1. ARAMA ÇUBUĞU (SEARCH BAR)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_city_or_region),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. VERİ KAYNAĞI SEÇİCİ (KANDİLLİ, AFAD, EMSC)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(EarthquakeSource.entries.toTypedArray()) { source ->
                val isSelected = uiState.selectedSource == source
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onSourceChanged(source) },
                    label = {
                        Text(
                            text = source.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. SAAT / ZAMAN FİLTRELERİ (ŞIK RAHAT SATIR)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(timeFilters) { (filterKey, labelText) ->
                val isSelected = uiState.selectedTimeFilter == filterKey
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { viewModel.onTimeFilterSelected(filterKey) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. BÜYÜKLÜK / DERECE FİLTRELERİ (ŞIK RAHAT SATIR)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(magFilters) { (mag, labelText) ->
                val isSelected = selectedMagFilter == mag
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedMagFilter = mag },
                    color = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SAYAC VE DURUM BİLGİSİ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.record_count, filteredList.size),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LİSTELEME VEYA YÜKLENİYOR / BOŞ DURUMU
        if (uiState.isLoading) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .shimmer()
                    )
                }
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.location_no_result),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                itemsIndexed(
                    items = filteredList,
                    key = { _, item -> item.id }
                ) { index, eq ->
                    AppAnimations.SpringEntranceContainer {
                        EarthquakeCard(
                            earthquake = eq,
                            onClick = { onEarthquakeClick(eq) }
                        )
                    }

                    if (shouldShowAdAtIndex(index, filteredList.size)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        NativeAdCard()
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EarthquakeCard(
    earthquake: Earthquake,
    onClick: () -> Unit
) {
    val (magBgColor, magTextColor) = when {
        earthquake.magnitude >= 5.0 -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        earthquake.magnitude >= 3.5 -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        earthquake.magnitude >= 2.0 -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = earthquake.cityImageUrl.ifEmpty { "https://picsum.photos/400/250?random=${earthquake.id}" },
                contentDescription = earthquake.location,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = earthquake.location,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${earthquake.region} • ${earthquake.date} ${earthquake.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = stringResource(R.string.source_label, earthquake.source),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(magBgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%.1f", earthquake.magnitude),
                    color = magTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}