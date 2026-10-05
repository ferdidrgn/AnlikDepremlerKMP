package com.ferdidrgn.anlikdepremler.ui.screen

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.data.EarthquakeCommentRepository
import com.ferdidrgn.anlikdepremler.core.data.EventPresenceRepository
import com.ferdidrgn.anlikdepremler.core.data.FeltReportRepository
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.core.share.shareEarthquakeAsImageCard
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.core.util.EmergencySmsHelper
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EarthquakeDetailScreen(
    earthquake: Earthquake,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isWhistleBlowing by remember { mutableStateOf(false) }

    DisposableEffect(isWhistleBlowing) {
        var toneGenerator: ToneGenerator? = null
        if (isWhistleBlowing) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onDispose {
            try {
                toneGenerator?.stopTone()
                toneGenerator?.release()
                toneGenerator = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val magnitudeColor = magnitudeHeatColor(earthquake.magnitude)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. HARİTA ÜST HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    LatLng(earthquake.latitude, earthquake.longitude), 9f
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                Circle(
                    center = LatLng(earthquake.latitude, earthquake.longitude),
                    radius = (earthquake.magnitude * 7500),
                    fillColor = magnitudeColor.copy(alpha = 0.25f),
                    strokeColor = magnitudeColor,
                    strokeWidth = 3f
                )

                val markerState = rememberMarkerState(
                    key = earthquake.id,
                    position = LatLng(earthquake.latitude, earthquake.longitude)
                )

                Marker(
                    state = markerState,
                    title = earthquake.location
                )
            }

            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // 2. BÖLGE RESMİ & LOKASYON KART
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = earthquake.cityImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = earthquake.location,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${earthquake.region} • ${earthquake.date} ${earthquake.time}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(magnitudeColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%.1f", earthquake.magnitude),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. MODELDEKİ ALANLAR
            AppAnimations.StaggeredEntrance(index = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailInfoTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.label_depth),
                        value = "${earthquake.depth} km",
                        icon = Icons.Default.Layers
                    )
                    DetailInfoTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.label_perceived_intensity),
                        value = "Mercalli ${earthquake.intensity}",
                        icon = Icons.Default.GraphicEq
                    )
                    DetailInfoTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.label_source),
                        value = earthquake.source,
                        icon = Icons.Default.Sensors
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppAnimations.StaggeredEntrance(index = 1) {
                SeismicImpactCard(
                    earthquakeId = earthquake.id,
                    magnitude = earthquake.magnitude,
                    depth = earthquake.depth
                )
            }

            // 4. HAYAT KURTARICI DÜDÜĞÜ
            AppAnimations.StaggeredEntrance(index = 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.whistle_title),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = stringResource(R.string.whistle_desc),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = {
                                EmergencySmsHelper.sendEmergencySms(
                                    context = context,
                                    phoneNumber = "",
                                    latitude = earthquake.latitude,
                                    longitude = earthquake.longitude,
                                    isSafe = false
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Konumlu SMS Gönder")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. PAYLAŞ BUTONU
            AppAnimations.StaggeredEntrance(index = 3) {
                OutlinedButton(
                    onClick = { shareEarthquakeAsImageCard(context, earthquake) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share_earthquake_info))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. GÖZ ŞAHİTLİĞİ YORUMLARI
            AppAnimations.StaggeredEntrance(index = 4) {
                EyewitnessCommentsSection(earthquakeId = earthquake.id)
            }
        }
    }
}

@Composable
private fun EyewitnessCommentsSection(earthquakeId: String) {
    val repository: EarthquakeCommentRepository = koinInject()
    val presenceRepository: EventPresenceRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val comments by repository.observeComments(earthquakeId).collectAsState(initial = emptyList())
    val viewerCount by presenceRepository.observeViewerCount(earthquakeId).collectAsState(initial = 0)
    var commentInput by remember { mutableStateOf("") }
    var reportedThisSession by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(earthquakeId) {
        while (true) {
            presenceRepository.heartbeat(earthquakeId)
            delay(EventPresenceRepository.HEARTBEAT_INTERVAL_MILLIS)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.comments_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (viewerCount > 0) {
                    Text(
                        text = stringResource(R.string.comments_viewer_count, viewerCount),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = {
                        if (it.length <= EarthquakeCommentRepository.MAX_COMMENT_LENGTH) commentInput = it
                    },
                    placeholder = { Text(stringResource(R.string.comments_input_hint), fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = commentInput.trim()
                        if (text.isNotEmpty()) {
                            commentInput = ""
                            coroutineScope.launch { repository.submitComment(earthquakeId, text) }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = stringResource(R.string.comments_send))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (comments.isEmpty()) {
                Text(
                    text = stringResource(R.string.comments_empty),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                comments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = comment.text,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                reportedThisSession = reportedThisSession + comment.id
                                coroutineScope.launch { repository.reportComment(earthquakeId, comment.id) }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = stringResource(R.string.comments_report),
                                tint = if (comment.id in reportedThisSession) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
private fun DetailInfoTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun SeismicImpactCard(
    earthquakeId: String,
    magnitude: Double,
    depth: Double
) {
    val repository: FeltReportRepository = koinInject()
    val preferencesManager: PreferencesManager = koinInject()
    val coroutineScope = rememberCoroutineScope()

    val currentFeltCount by repository.observeFeltCount(earthquakeId).collectAsState(initial = 0L)
    val feltReportedIds by preferencesManager.feltReportedEarthquakeIds.collectAsState(initial = emptySet())
    val hasUserFelt = earthquakeId in feltReportedIds

    // 🎯 REMEMBER SARMALI İLE STATE DERLEYİCİ HATASI ÇÖZÜLDÜ
    val mmiText = remember(magnitude, depth) {
        when {
            magnitude >= 6.0 && depth <= 15.0 -> "MMI IX (X)"
            magnitude >= 6.0 -> "MMI VIII"
            magnitude >= 4.5 && depth <= 10.0 -> "MMI VI (VII)"
            magnitude >= 4.5 -> "MMI V"
            magnitude >= 3.0 && depth <= 15.0 -> "MMI IV"
            magnitude >= 3.0 -> "MMI III"
            else -> "MMI I (II)"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_summary_subtitle),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currentFeltCount ${stringResource(R.string.unit_earthquake)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        if (!hasUserFelt) {
                            coroutineScope.launch {
                                preferencesManager.markEarthquakeAsFelt(earthquakeId)
                                repository.submitFeltReport(earthquakeId)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasUserFelt) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (hasUserFelt) "✓" else stringResource(R.string.show_more),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${stringResource(R.string.label_depth)}: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$depth km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (depth <= 10.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${stringResource(R.string.label_perceived_intensity)}: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mmiText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

