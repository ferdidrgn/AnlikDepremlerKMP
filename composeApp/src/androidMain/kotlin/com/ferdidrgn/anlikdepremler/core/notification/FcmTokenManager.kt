package com.ferdidrgn.anlikdepremler.core.notification

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

/** How long an inactive device's doc survives before Firestore's TTL policy (configured in the
 *  Firebase Console on the "expiresAt" field) deletes it. Re-pushed forward on every app open, so
 *  only a genuinely abandoned/uninstalled device's doc ever reaches the past. */
private val DEVICE_DOC_TTL_MILLIS = TimeUnit.DAYS.toMillis(90)

object FcmTokenManager {

    fun syncFcmToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            touchDeviceDoc(context, token)
        }
    }

}

/** Writes (or refreshes) this device's doc every app cold start - one small write, not polling -
 *  so "lastActive"/"expiresAt" stay current for devices still in use. */
private fun touchDeviceDoc(context: Context, token: String) {
    val db = FirebaseFirestore.getInstance()

    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var deviceId = sharedPrefs.getString("device_id", null)
    if (deviceId == null) {
        deviceId = UUID.randomUUID().toString()
        sharedPrefs.edit { putString("device_id", deviceId) }
    }

    val now = Timestamp.now()
    val expiresAt = Timestamp(Date(System.currentTimeMillis() + DEVICE_DOC_TTL_MILLIS))

    val deviceData = hashMapOf(
        "fcmToken" to token,
        "deviceId" to deviceId,
        "deviceModel" to Build.MODEL,
        "osVersion" to Build.VERSION.RELEASE,
        "lastActive" to now,
        "expiresAt" to expiresAt
    )

    db.collection("devices").document(deviceId)
        .set(deviceData, SetOptions.merge())
}