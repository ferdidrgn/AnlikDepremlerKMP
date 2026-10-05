package com.ferdidrgn.anlikdepremler.core.ads

import android.app.Activity
import android.content.Context
import com.ferdidrgn.anlikdepremler.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/** Loads/shows a rewarded video. Currently used to grant +1 extra saved-location slot
 *  beyond the normal cap - repurposed from "offline map tile packs", which Google Maps
 *  Platform's Terms of Service don't allow apps to cache for arbitrary offline use. */
class RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun load(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    val isReady: Boolean get() = rewardedAd != null

    fun show(activity: Activity, onEarned: () -> Unit, onDismissed: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onDismissed()
            return
        }
        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                load(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                load(activity)
                onDismissed()
            }
        }
        ad.show(activity) { onEarned() }
    }
}
