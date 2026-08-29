package com.ferdidrgn.anlikdepremler

import android.app.Application
import com.ferdidrgn.anlikdepremler.core.ads.AdManager
import com.ferdidrgn.anlikdepremler.core.notification.FcmTokenManager
import com.ferdidrgn.anlikdepremler.di.androidPlatformModules
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EarthquakeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            startKoin {
                androidLogger()
                androidContext(this@EarthquakeApplication)
                modules(androidPlatformModules)
            }

            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
            FcmTokenManager.syncFcmToken(this)

            val adManager: AdManager = get()
            adManager.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
