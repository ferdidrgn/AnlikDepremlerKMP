package com.ferdidrgn.anlikdepremler.core.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.content.edit

object LocaleUtils {

    fun setAppLanguage(context: Context, languageCode: String) {
        // 1. AppCompatDelegate üzerinden varsayılan uygulama dilini güncelliyoruz
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

        // 2. Eski SharedPreferences uyumluluğu (MainActivity attachBaseContext için)
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("selected_language", languageCode) }

        // 3. Ekranın ve tüm stringResource'ların anında yeni dile dönmesi için Activity'yi yeniden başlatıyoruz
        (context as? Activity)?.recreate()
    }
}