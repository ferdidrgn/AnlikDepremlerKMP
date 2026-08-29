package com.ferdidrgn.anlikdepremler.core.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewHelper {

    fun launchInAppReview(context: Context) {
        val activity = context as? Activity ?: run {
            openPlayStorePage(context)
            return
        }

        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // Kullanıcı değerlendirmeyi tamamladı veya kapattı
                }
            } else {
                // In-App Review başarısız olursa (örn. Debug/APK çalışırken) doğrudan Play Store sayfasına yönlendir
                openPlayStorePage(context)
            }
        }
    }

    // Doğrudan Google Play Store Oylama Sayfasını Açan Fonksiyon
    fun openPlayStorePage(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}