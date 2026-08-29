package com.ferdidrgn.anlikdepremler.core.notification

import android.content.Context
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID
import androidx.core.content.edit

object FcmTokenManager {

    fun syncFcmToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            val token = task.result ?: return@addOnCompleteListener

            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val lastSavedToken = sharedPrefs.getString("fcm_token", null)

            // Eğer token değişmediyse tekrar Firestore'a yazıp kotayı tüketme
            if (lastSavedToken != token) {
                saveTokenToFirestore(context, token) {
                    sharedPrefs.edit { putString("fcm_token", token) }
                }
            }
        }
    }

}

private fun saveTokenToFirestore(context: Context, token: String, onSuccess: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var deviceId = sharedPrefs.getString("device_id", null)
    if (deviceId == null) {
        deviceId = UUID.randomUUID().toString()
        sharedPrefs.edit { putString("device_id", deviceId) }
    }

    val deviceData = hashMapOf(
        "fcmToken" to token,
        "deviceId" to deviceId,
        "deviceModel" to Build.MODEL,
        "osVersion" to Build.VERSION.RELEASE,
        "lastActive" to System.currentTimeMillis()
    )

    db.collection("devices").document(deviceId)
        .set(deviceData)
        .addOnSuccessListener {
            onSuccess()
        }
}