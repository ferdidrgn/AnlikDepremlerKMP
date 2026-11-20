package com.ferdidrgn.anlikdepremler.core.notification

import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Date
import java.util.concurrent.TimeUnit

/** How long an inactive device's doc survives before Firestore's TTL policy (configured in the
 *  Firebase Console on the "expiresAt" field) deletes it. Re-pushed forward on every app open, so
 *  only a genuinely abandoned/uninstalled device's doc ever reaches the past. */
private val DEVICE_DOC_TTL_MILLIS = TimeUnit.DAYS.toMillis(90)

object FcmTokenManager {

    fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            syncFcmToken(token)
        }
    }

    fun syncFcmToken(token: String) {
        if (token.isBlank()) return
        touchDeviceDoc(token)
    }

}

/** Writes (or refreshes) this device's doc every app cold start - one small write, not polling -
 *  so "lastActive"/"expiresAt" stay current for devices still in use. */
private fun touchDeviceDoc(token: String) {
    val db = FirebaseFirestore.getInstance()
    val deviceId = FirebaseAuth.getInstance().currentUser?.uid ?: return

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
