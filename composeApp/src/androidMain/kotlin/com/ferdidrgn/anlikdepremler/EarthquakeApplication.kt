package com.ferdidrgn.anlikdepremler

import android.app.Application
import com.ferdidrgn.anlikdepremler.core.notification.FcmTokenManager
import com.ferdidrgn.anlikdepremler.di.androidPlatformModules
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                if (BuildConfig.DEBUG) {
                    DebugAppCheckProviderFactory.getInstance()
                } else {
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                },
                true
            )
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                FcmTokenManager.syncFcmToken()
            } else {
                auth.signInAnonymously().addOnSuccessListener {
                    FcmTokenManager.syncFcmToken()
                }.addOnFailureListener { error ->
                    FirebaseCrashlytics.getInstance().recordException(error)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
