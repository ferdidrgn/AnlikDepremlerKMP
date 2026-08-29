package com.ferdidrgn.anlikdepremler.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object EmergencySmsHelper {

    fun sendEmergencySms(
        context: Context,
        phoneNumber: String = "",
        latitude: Double?,
        longitude: Double?,
        isSafe: Boolean
    ) {
        val locationLink = if (latitude != null && longitude != null) {
            "https://maps.google.com/?q=$latitude,$longitude"
        } else {
            "Konum alınamadı."
        }

        val statusText = if (isSafe) {
            "GÜVENDEYİM! Deprem sonrası durumum iyi."
        } else {
            "ACİL YARDIM! Deprem bölgesindeyim, yardıma ihtiyacım var."
        }

        val message = "🚨 ACİL DURUM BİLDİRİMİ\n\n$statusText\n\n📍 Son Konumum: $locationLink"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    }
}