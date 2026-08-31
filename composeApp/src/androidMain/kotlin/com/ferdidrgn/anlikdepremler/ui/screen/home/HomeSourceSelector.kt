package com.ferdidrgn.anlikdepremler.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource

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
