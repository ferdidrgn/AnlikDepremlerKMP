package com.ferdidrgn.anlikdepremler.core.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleUtils {

    /**
     * AppCompatDelegate self-persists the per-app locale (Android 13+ system settings,
     * or an internal store on older versions) and re-applies it automatically on every
     * future launch — no manual SharedPreferences/attachBaseContext bookkeeping needed.
     */
    fun setAppLanguage(context: Context, languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        (context as? Activity)?.recreate()
    }
}