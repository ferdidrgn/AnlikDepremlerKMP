package com.ferdidrgn.anlikdepremler.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun getAppVersionName(context: Context): String {
    return try {
        val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }
        "v$versionName • Professional Edition"
    } catch (e: Exception) {
        "v1.31 • Professional Edition"
    }
}