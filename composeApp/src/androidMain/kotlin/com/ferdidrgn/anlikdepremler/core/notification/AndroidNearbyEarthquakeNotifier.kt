package com.ferdidrgn.anlikdepremler.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.MainActivity
import com.ferdidrgn.anlikdepremler.R
import kotlin.math.roundToInt

/**
 * Posts a system notification from on-device data (location + already-fetched earthquake list),
 * reusing the channel/PendingIntent pattern from EarthquakeFirebaseMessagingService but on a
 * separate channel so the user can mute "nearby" alerts independently of the critical FCM ones.
 */
class AndroidNearbyEarthquakeNotifier(private val context: Context) : NearbyEarthquakeNotifier {

    override fun notifyNearbyEarthquake(earthquake: Earthquake, distanceKm: Double) {
        val channelId = "nearby_earthquake_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Yakın Deprem Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Konumunuza yakın bölgelerdeki depremler için anlık bildirimler"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("anlikdepremler://detail/${earthquake.id}"),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            earthquake.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Yakınınızda Deprem 🚨"
        val body = "${earthquake.location}: ${earthquake.magnitude} büyüklüğünde, " +
            "${distanceKm.roundToInt()} km uzaklıkta deprem oldu."

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(earthquake.id.hashCode(), builder.build())
    }
}
