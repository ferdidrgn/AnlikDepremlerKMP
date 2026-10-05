package com.ferdidrgn.anlikdepremler.core.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ferdidrgn.anlikdepremler.BuildConfig
import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import org.koin.compose.koinInject

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val preferencesManager: PreferencesManager = koinInject()
    val adsFreeUntilMillis by preferencesManager.adsFreeUntilMillis.collectAsState(initial = 0L)
    if (adsFreeUntilMillis > System.currentTimeMillis()) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}