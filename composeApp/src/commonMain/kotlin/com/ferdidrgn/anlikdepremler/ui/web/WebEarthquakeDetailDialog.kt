package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.ui.components.RemoteImage
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor

/**
 * Every onClick on an earthquake row/card was a no-op ({}) - tapping one did nothing at all.
 * Not porting Android's full EarthquakeDetailScreen here (comments, felt-it counter, presence -
 * all backed by Firestore via the Android Firebase SDK, which isn't wired up for wasmJs in this
 * project) - this is a read-only detail view, the piece that's purely a client-side gap.
 */
@Composable
fun WebEarthquakeDetailDialog(earthquake: Earthquake, onDismiss: () -> Unit) {
    val magnitudeColor = magnitudeHeatColor(earthquake.magnitude)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 420.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Box {
                    RemoteImage(
                        url = earthquake.cityImageUrl,
                        contentDescription = earthquake.location,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        placeholder = {
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(magnitudeColor.copy(alpha = 0.2f)))
                        }
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = magnitudeColor.copy(alpha = 0.15f)) {
                            Text(
                                text = "${formatDecimal(earthquake.magnitude)} Mw",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = magnitudeColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            earthquake.intensity.let { "Şiddet: $it" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(earthquake.location, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.height(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            earthquake.region,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    DetailRow("Tarih / Saat", "${earthquake.date} ${earthquake.time}")
                    DetailRow("Derinlik", "${formatDecimal(earthquake.depth)} km")
                    DetailRow("Enlem / Boylam", "${formatDecimal(earthquake.latitude)}, ${formatDecimal(earthquake.longitude)}")
                    DetailRow("Kaynak", earthquake.source)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatDecimal(value: Double): String = (kotlin.math.round(value * 10) / 10.0).toString()
