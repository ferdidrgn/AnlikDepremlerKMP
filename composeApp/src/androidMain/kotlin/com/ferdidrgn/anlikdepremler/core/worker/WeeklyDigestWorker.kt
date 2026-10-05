package com.ferdidrgn.anlikdepremler.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ferdidrgn.anlikdepremler.MainActivity
import com.ferdidrgn.anlikdepremler.R
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import com.ferdidrgn.anlikdepremler.domain.util.calculateStatistics
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Runs weekly (see WeeklyDigestScheduler) and posts a local notification summarizing the last
 * 7 days - reuses the same calculateStatistics() extension HomeScreen already uses for its
 * stats section, so "weekCount/maxMagnitude/mostActiveRegion" stay consistent with what the app
 * shows elsewhere. Deliberately not location-filtered (unlike the nearby-earthquake checks):
 * a background worker requesting a fresh GPS fix just for a once-a-week summary isn't worth the
 * permission/battery cost, so this gives a general national summary instead.
 */
class WeeklyDigestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: EarthquakeRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val earthquakes = repository.getEarthquakes(EarthquakeSource.KANDILLI).first()
            if (earthquakes.isEmpty()) return Result.success()

            val stats = earthquakes.calculateStatistics()
            if (stats.totalWeek <= 0) return Result.success()

            showDigestNotification(stats.totalWeek, stats.maxMagnitude, stats.mostActiveRegion)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showDigestNotification(weekCount: Int, maxMagnitude: Double, mostActiveRegion: String) {
        val context = applicationContext
        val channelId = "weekly_digest_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Haftalık Deprem Özeti",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Haftalık deprem aktivitesi özeti bildirimi"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = context.getString(
            R.string.weekly_digest_body,
            weekCount,
            mostActiveRegion,
            maxMagnitude
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.weekly_digest_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 9001
        const val WORK_NAME = "weekly_earthquake_digest"
    }
}
