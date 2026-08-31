package com.ferdidrgn.anlikdepremler.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdi.deprem.model.EarthquakeStatistics
import com.ferdidrgn.anlikdepremler.R

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
private fun StatCard(
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
