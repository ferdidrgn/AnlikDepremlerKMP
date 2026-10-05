package com.ferdidrgn.anlikdepremler.core.notification

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Speaks a short earthquake summary aloud via Android's built-in TextToSpeech engine - opt-in
 * (see PreferencesManager.voiceAlertsEnabled), used by both the on-device nearby-earthquake
 * notifier and the critical FCM push handler so an alert can be heard even if the phone is
 * face-down or out of sight. One instance is shared app-wide (Koin single) since initializing
 * the TTS engine is slow enough that doing it per-notification would delay the announcement.
 */
class EarthquakeVoiceAnnouncer(context: Context) {
    private var isReady = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("tr", "TR")
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "earthquake_voice_alert")
    }
}
