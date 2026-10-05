package com.ferdidrgn.anlikdepremler.core.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Starts/stops the weekly digest's periodic work - a 7-day cadence is light enough that
 *  WorkManager's standard periodic-work mechanism is the right tool here, unlike the
 *  every-few-minutes polling this app deliberately keeps foreground-only elsewhere. */
object WeeklyDigestScheduler {

    fun enable(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeeklyDigestWorker>(7, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeeklyDigestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun disable(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WeeklyDigestWorker.WORK_NAME)
    }
}
