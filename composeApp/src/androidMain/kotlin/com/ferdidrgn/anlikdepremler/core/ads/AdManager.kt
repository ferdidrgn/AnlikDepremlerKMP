package com.ferdidrgn.anlikdepremler.core.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ferdidrgn.anlikdepremler.BuildConfig

class AdManager {

    private var interstitialAd: InterstitialAd? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: android.app.Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.show(activity)
            interstitialAd = null
            loadInterstitial(activity) // Bir sonraki için tekrar yükle
            onAdDismissed()
        } else onAdDismissed()
    }
}