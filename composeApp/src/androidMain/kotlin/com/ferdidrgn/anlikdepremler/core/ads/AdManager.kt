package com.ferdidrgn.anlikdepremler.core.ads

import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ferdidrgn.anlikdepremler.BuildConfig

class AdManager {

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var isLoading = false
    private var lastShownAtMillis = 0L

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

    fun loadInterstitial(context: Context) {
        if (!isInitialized || interstitialAd != null || isLoading) return
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showInterstitial(activity: android.app.Activity, onAdDismissed: () -> Unit) {
        val now = SystemClock.elapsedRealtime()
        val ad = interstitialAd
        if (ad == null || (lastShownAtMillis != 0L && now - lastShownAtMillis < MIN_INTERVAL_MILLIS)) {
            onAdDismissed()
            return
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastShownAtMillis = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                loadInterstitial(activity.applicationContext)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadInterstitial(activity.applicationContext)
                onAdDismissed()
            }
        }
        ad.show(activity)
    }

    private companion object {
        const val MIN_INTERVAL_MILLIS = 3 * 60 * 1000L
    }
}
