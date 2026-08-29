package com.ferdidrgn.anlikdepremler.core.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsLogger {

    // Özel Hata Kaydı Gönderme (Fatal Olmayan Hatalar İçin)
    fun logException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    // Özel Mesaj Ekleme (Hatanın Nerede Olduğunu Anlamak İçin Log)
    fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    // Kullanıcıya / Cihaza Özel Anahtar-Değer Kaydetme
    fun setCustomKey(key: String, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }
}