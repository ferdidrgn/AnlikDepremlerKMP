package com.ferdidrgn.anlikdepremler.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.core.util.EmergencySmsHelper

@Composable
fun NearbyEarthquakeAlertCard(
    earthquake: Earthquake,
    emergencyPhone: String,
    onSafeClicked: () -> Unit,
    onNeedHelpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "🚨 ${stringResource(R.string.nearby_earthquake_title)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "${earthquake.location} (${earthquake.magnitude} Mw)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.nearby_earthquake_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        EmergencySmsHelper.sendEmergencySms(
                            context = context,
                            phoneNumber = emergencyPhone,
                            latitude = earthquake.latitude,
                            longitude = earthquake.longitude,
                            isSafe = true
                        )
                        onSafeClicked()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "🟢 ${stringResource(R.string.btn_im_safe)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        EmergencySmsHelper.sendEmergencySms(
                            context = context,
                            phoneNumber = emergencyPhone,
                            latitude = earthquake.latitude,
                            longitude = earthquake.longitude,
                            isSafe = false
                        )
                        onNeedHelpClicked()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "🚨 ${stringResource(R.string.btn_need_help)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}