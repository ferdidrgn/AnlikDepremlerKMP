package com.ferdidrgn.anlikdepremler.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ferdidrgn.anlikdepremler.MainActivity
import com.ferdidrgn.anlikdepremler.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EarthquakeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
        ?: "Sarsıntı Bildirimi 🚨"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body
        ?: "Yeni bir sismik hareketlilik algılandı."
        val earthquakeId = remoteMessage.data["earthquakeId"]
        val magnitude = remoteMessage.data["magnitude"]?.toDoubleOrNull() ?: 0.0

        // 🎯 Yalnızca 4.0 ve üzeri depremlerde bildirim göster
        if (magnitude >= 4.0 || remoteMessage.data["isCritical"] == "true") {
            showNotification(title, body, earthquakeId)
        }
    }

    private fun showNotification(title: String, body: String, earthquakeId: String?) {
        val channelId = "earthquake_critical_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Kritik Deprem Uyarısı",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "4.0 Mw ve üzeri depremler için anlık bildirimler"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Deeplink Intent (detail/{earthquakeId})
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("anlikdepremler://detail/${earthquakeId ?: ""}"),
            this,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Firebase Token güncellemesi yapılabilir
    }
}