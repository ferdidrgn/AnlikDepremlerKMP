package com.ferdidrgn.anlikdepremler.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.ferdidrgn.anlikdepremler.R

/** System [Intent] launchers backing the Settings screen's action tiles. */

fun sendEmailIntent(context: Context, email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        "mailto:$email".toUri().also { data = it }
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
    }
    context.startActivity(Intent.createChooser(intent, null))
}

fun openNotificationSettings(context: Context) {
    val intent = Intent().apply {
        action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Settings.ACTION_APP_NOTIFICATION_SETTINGS
        else Settings.ACTION_APPLICATION_DETAILS_SETTINGS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}

fun openWebPage(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}

fun shareApp(context: Context) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(
            Intent.EXTRA_TEXT,
            "${context.getString(R.string.app_name)}: https://play.google.com/store/apps/details?id=${context.packageName}"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}
