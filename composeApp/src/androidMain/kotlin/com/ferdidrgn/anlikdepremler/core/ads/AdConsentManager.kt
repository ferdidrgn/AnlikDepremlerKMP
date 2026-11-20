package com.ferdidrgn.anlikdepremler.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps every ad request behind the current UMP consent state. */
class AdConsentManager(context: Context) {

    private val consentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)
    private val _canRequestAds = MutableStateFlow(false)

    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(activity: Activity, onComplete: (Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    finishConsentFlow(onComplete)
                }
            },
            {
                // A previous valid decision may still allow ads when the refresh fails.
                finishConsentFlow(onComplete)
            }
        )
    }

    fun showPrivacyOptions(activity: Activity, onDismissed: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            _canRequestAds.value = consentInformation.canRequestAds()
            onDismissed()
        }
    }

    private fun finishConsentFlow(onComplete: (Boolean) -> Unit) {
        val allowed = consentInformation.canRequestAds()
        _canRequestAds.value = allowed
        onComplete(allowed)
    }
}
