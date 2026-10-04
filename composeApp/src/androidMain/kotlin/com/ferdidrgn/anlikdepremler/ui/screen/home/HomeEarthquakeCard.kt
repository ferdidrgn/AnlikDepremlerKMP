package com.ferdidrgn.anlikdepremler.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.ui.animation.AppAnimations
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor

@Composable
fun ExpandableEarthquakeCard(
    earthquake: Earthquake,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val magnitudeColor = magnitudeHeatColor(earthquake.magnitude)

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
