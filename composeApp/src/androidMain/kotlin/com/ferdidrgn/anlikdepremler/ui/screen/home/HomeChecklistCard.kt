package com.ferdidrgn.anlikdepremler.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class ChecklistItem(
    val id: String,
    val titleRes: Int,
    val descRes: Int
)

private val CHECKLIST_ITEMS = listOf(
    ChecklistItem("food", R.string.checklist_item_food_title, R.string.checklist_item_food_desc),
    ChecklistItem("flashlight", R.string.checklist_item_flashlight_title, R.string.checklist_item_flashlight_desc),
    ChecklistItem("firstaid", R.string.checklist_item_firstaid_title, R.string.checklist_item_firstaid_desc),
    ChecklistItem("documents", R.string.checklist_item_documents_title, R.string.checklist_item_documents_desc),
    ChecklistItem("powerbank", R.string.checklist_item_powerbank_title, R.string.checklist_item_powerbank_desc),
    ChecklistItem("whistle", R.string.checklist_item_whistle_title, R.string.checklist_item_whistle_desc)
)

/** Emergency-kit checklist, now with 6 real items and a persisted completion score instead of
 *  the single-item, never-saved toggle this used to be. */
@Composable
fun QuickChecklistCard() {
    val preferencesManager: PreferencesManager = koinInject()
    val completedIds by preferencesManager.completedChecklistItems.collectAsState(initial = emptySet())
    val coroutineScope = rememberCoroutineScope()

    val completedCount = CHECKLIST_ITEMS.count { it.id in completedIds }
    val allComplete = completedCount == CHECKLIST_ITEMS.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.checklist_progress, completedCount, CHECKLIST_ITEMS.size),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { completedCount / CHECKLIST_ITEMS.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary
            )

            AnimatedVisibility(visible = allComplete) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.checklist_complete_badge),
                        modifier = Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            CHECKLIST_ITEMS.forEach { item ->
                ChecklistItemRow(
                    item = item,
                    checked = item.id in completedIds,
                    onCheckedChange = { checked ->
                        coroutineScope.launch {
                            preferencesManager.setChecklistItemCompleted(item.id, checked)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val checkScale = remember { Animatable(1f) }
    LaunchedEffect(checked) {
        if (checked) {
            checkScale.snapTo(0.7f)
            checkScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(item.titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                stringResource(item.descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.scale(checkScale.value)
        )
    }
}
