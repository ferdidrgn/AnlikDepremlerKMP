package com.ferdidrgn.anlikdepremler.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ferdidrgn.anlikdepremler.R

enum class AppPermissionRequestMode {
    ALL_OPTIONAL,
    LOCATION_ONLY
}

@Composable
fun RequestAppPermissions(
    onPermissionsGranted: () -> Unit,
    onSkip: () -> Unit,
    mode: AppPermissionRequestMode = AppPermissionRequestMode.ALL_OPTIONAL
) {
    val context = LocalContext.current

    val permissionsToRequest = remember(mode) {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (mode == AppPermissionRequestMode.ALL_OPTIONAL &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun checkRequestedPermissionsGranted(): Boolean {
        val hasLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val hasNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        return hasLocation &&
            (mode == AppPermissionRequestMode.LOCATION_ONLY || hasNotifications)
    }

    var showDialog by remember(mode) { mutableStateOf(!checkRequestedPermissionsGranted()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (checkRequestedPermissionsGranted()) {
            showDialog = false
            onPermissionsGranted()
        }
    }

    LaunchedEffect(mode) {
        if (checkRequestedPermissionsGranted()) {
            onPermissionsGranted()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onSkip,
            icon = {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(
                        if (mode == AppPermissionRequestMode.LOCATION_ONLY) {
                            R.string.location_permissions
                        } else {
                            R.string.permissions_title
                        }
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = stringResource(
                        if (mode == AppPermissionRequestMode.LOCATION_ONLY) {
                            R.string.location_permissions_sub
                        } else {
                            R.string.permissions_desc
                        }
                    ),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionLauncher.launch(permissionsToRequest)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_grant_permissions))
                }
            },
            dismissButton = {
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.permissions_not_now))
                }
            }
        )
    }
}
