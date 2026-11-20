package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.domain.util.parseEarthquakeInstant
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

private const val HOUR_MILLIS = 60L * 60L * 1000L
private const val TWO_HOURS_MILLIS = 2L * HOUR_MILLIS
private const val DAY_MILLIS = 24L * HOUR_MILLIS

/** Descriptive activity only; this is not a forecast or an aftershock prediction. */
@Composable
fun WebActivityInsights(earthquakes: List<Earthquake>, modifier: Modifier = Modifier) {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toEpochMilliseconds()
    val recent = remember(earthquakes, now, timeZone) {
        earthquakes.mapNotNull { event ->
            parseEarthquakeInstant(event.date, event.time, timeZone)
                ?.toEpochMilliseconds()
                ?.takeIf { it in (now - DAY_MILLIS)..now }
                ?.let { event to it }
        }
    }
    val timeline = remember(recent, now) {
        (11 downTo 0).map { bin ->
            val count = recent.count { (_, timestamp) ->
                ((now - timestamp) / TWO_HOURS_MILLIS).toInt() == bin
            }
            ActivityBin(label = if (bin == 0) "şimdi" else "${bin * 2}s", count = count)
        }
    }
    val regions = remember(recent) {
        recent.groupingBy { (event, _) ->
            event.region.takeIf { it.isNotBlank() && it.uppercase() !in setOf("GLOBAL", "DÜNYA", "EMSC - EU") }
                ?: event.location
        }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(4)
    }
    val maxBinCount = timeline.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val maxRegionCount = regions.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Son 24 saat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Yüklenen kayıtlara göre olay dağılımı · tahmin veya erken uyarı değildir",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                timeline.forEach { bin ->
                    val targetHeight = (8 + (bin.count.toFloat() / maxBinCount) * 54).dp
                    val height by animateDpAsState(targetHeight, label = "activity-${bin.label}")
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(bin.count.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth(0.72f).height(height)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (bin.label == "şimdi") 0.95f else 0.64f))
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(bin.label, style = MaterialTheme.typography.labelSmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Kayıt yoğunluğu görülen bölgeler", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (regions.isEmpty()) {
                Text("Son 24 saatte gösterilecek kayıt yok.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                regions.forEach { (region, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(region, modifier = Modifier.width(120.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        val targetWidth = (count.toFloat() / maxRegionCount).coerceIn(0.04f, 1f)
                        Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface)) {
                            Box(Modifier.fillMaxWidth(targetWidth).height(8.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondary))
                        }
                        Text(count.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Bölge sayıları yalnızca bu ekranda yüklenen son 24 saatlik olayların özetidir.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class ActivityBin(val label: String, val count: Int)
