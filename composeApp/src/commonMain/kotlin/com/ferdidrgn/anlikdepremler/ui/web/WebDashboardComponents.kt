package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource

@Composable
fun WebBrandHeader(appName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LiveBadge()
        }
    }
}

@Composable
fun LiveBadge() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Circle,
            contentDescription = null,
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(8.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "Canlı veri",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WebSourceList(
    selected: EarthquakeSource,
    onSelected: (EarthquakeSource) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        EarthquakeSource.entries.forEach { source ->
            val isSelected = source == selected
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()

            Surface(
                onClick = { onSelected(source) },
                modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    else -> Color.Transparent
                }
            ) {
                Text(
                    text = source.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun WebStatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .hoverable(interactionSource)
            .height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 6.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent)
            )
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WebTimeFilterRow(selected: String, labels: Map<String, String>, onSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEach { (key, label) ->
            val isSelected = key == selected
            Surface(
                onClick = { onSelected(key) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WebEarthquakeTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WebTableHeaderCell("Büyüklük", Modifier.width(90.dp))
        WebTableHeaderCell("Konum", Modifier.weight(1f))
        WebTableHeaderCell("Bölge", Modifier.width(140.dp))
        WebTableHeaderCell("Tarih / Saat", Modifier.width(160.dp))
        WebTableHeaderCell("Derinlik", Modifier.width(90.dp))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@Composable
private fun WebTableHeaderCell(text: String, modifier: Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun WebEarthquakeRow(earthquake: Earthquake, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val magnitudeColor = when {
        earthquake.magnitude < 2.0 -> MaterialTheme.colorScheme.primary
        earthquake.magnitude < 3.5 -> MaterialTheme.colorScheme.secondary
        earthquake.magnitude < 5.0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clickable { onClick() }
            .background(if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.width(90.dp).wrapContentWidth(Alignment.Start),
            shape = RoundedCornerShape(8.dp),
            color = magnitudeColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = "${formatDecimal(earthquake.magnitude)} Mw",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = magnitudeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Text(
            earthquake.location,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            earthquake.region,
            modifier = Modifier.width(140.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${earthquake.date} ${earthquake.time}",
            modifier = Modifier.width(160.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${formatDecimal(earthquake.depth)} km",
            modifier = Modifier.width(90.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WebMagnitudeMiniChart(distribution: Map<String, Int>) {
    val maxValue = distribution.values.maxOrNull()?.toFloat() ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        distribution.forEach { (range, count) ->
            val percentage = (count.toFloat() / maxValue) * 100f
            val color = when (range) {
                "1-2" -> MaterialTheme.colorScheme.primary
                "2-3" -> MaterialTheme.colorScheme.secondary
                "3-4" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(range, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                WebAnimatedBar(percentage, color, Modifier.weight(1f).height(16.dp))
                Text(count.toString(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(20.dp))
            }
        }
    }
}

@Composable
private fun WebAnimatedBar(percentage: Float, color: Color, modifier: Modifier) {
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(percentage) {
        progress = 0f
        animate(0f, percentage, animationSpec = tween(600, easing = FastOutSlowInEasing)) { value, _ -> progress = value }
    }
    Box(modifier = modifier.background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((progress / 100f).coerceIn(0f, 1f))
                .background(color, RoundedCornerShape(4.dp))
        )
    }
}

/** Narrow-viewport equivalent of [WebEarthquakeRow] - the table's fixed-width columns don't fit a phone browser. */
@Composable
fun WebEarthquakeCardCompact(earthquake: Earthquake, onClick: () -> Unit) {
    val magnitudeColor = when {
        earthquake.magnitude < 2.0 -> MaterialTheme.colorScheme.primary
        earthquake.magnitude < 3.5 -> MaterialTheme.colorScheme.secondary
        earthquake.magnitude < 5.0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = magnitudeColor.copy(alpha = 0.15f)) {
                Text(
                    text = "${formatDecimal(earthquake.magnitude)} Mw",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = magnitudeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(earthquake.location, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${earthquake.region} • ${earthquake.date} ${earthquake.time}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("${formatDecimal(earthquake.depth)} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** String.format("%.1f", ...) isn't available outside the JVM; this is a common-code equivalent. */
private fun formatDecimal(value: Double): String = (kotlin.math.round(value * 10) / 10.0).toString()
