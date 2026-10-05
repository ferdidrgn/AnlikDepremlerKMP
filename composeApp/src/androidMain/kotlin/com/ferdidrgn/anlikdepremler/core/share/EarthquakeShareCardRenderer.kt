package com.ferdidrgn.anlikdepremler.core.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor

/**
 * Draws a shareable 1080x1350 PNG "story card" for an earthquake using plain android.graphics
 * Canvas calls - deliberately not a captured Composable, since off-screen Compose->Bitmap
 * capture needs a view attached to a window with a measured size, which is fragile from a
 * one-shot share action. Plain Canvas drawing has none of those lifecycle gotchas.
 */
object EarthquakeShareCardRenderer {
    private const val WIDTH = 1080
    private const val HEIGHT = 1350
    private const val CENTER_X = WIDTH / 2f

    fun render(earthquake: Earthquake): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(canvas)
        drawBranding(canvas)
        val magnitudeColor = magnitudeHeatColor(earthquake.magnitude).toArgb()
        drawMagnitudeCircle(canvas, earthquake, magnitudeColor)
        drawLocationDetails(canvas, earthquake)
        drawCallToAction(canvas)

        return bitmap
    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, HEIGHT.toFloat(),
                Color.parseColor("#0F172A"),
                Color.parseColor("#1E293B"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
    }

    private fun drawBranding(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("🌍 Anlık Depremler", 60f, 120f, paint)
    }

    private fun drawMagnitudeCircle(canvas: Canvas, earthquake: Earthquake, magnitudeColor: Int) {
        val centerY = 420f
        val radius = 220f

        canvas.drawCircle(CENTER_X, centerY, radius, Paint().apply {
            color = magnitudeColor
            isAntiAlias = true
        })

        canvas.drawText(
            String.format("%.1f", earthquake.magnitude),
            CENTER_X,
            centerY + 55f,
            Paint().apply {
                color = Color.WHITE
                textSize = 160f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
        )

        canvas.drawText(
            "büyüklüğünde",
            CENTER_X,
            centerY + radius + 70f,
            Paint().apply {
                color = Color.WHITE
                textSize = 36f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
        )
    }

    private fun drawLocationDetails(canvas: Canvas, earthquake: Earthquake) {
        canvas.drawText(
            earthquake.location,
            CENTER_X,
            880f,
            Paint().apply {
                color = Color.WHITE
                textSize = 58f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
        )

        canvas.drawText(
            "${earthquake.region} • ${earthquake.date} ${earthquake.time}",
            CENTER_X,
            935f,
            Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 38f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
        )

        canvas.drawText(
            "Derinlik: ${earthquake.depth} km   •   Kaynak: ${earthquake.source}",
            CENTER_X,
            985f,
            Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 34f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
        )
    }

    private fun drawCallToAction(canvas: Canvas) {
        val ctaRect = RectF(60f, HEIGHT - 180f, WIDTH - 60f, HEIGHT - 80f)
        canvas.drawRoundRect(ctaRect, 28f, 28f, Paint().apply {
            color = Color.parseColor("#22C55E")
            isAntiAlias = true
        })

        canvas.drawText(
            "Canlı deprem takibi: anlikdeprem.web.app",
            CENTER_X,
            HEIGHT - 118f,
            Paint().apply {
                color = Color.WHITE
                textSize = 38f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
        )
    }
}
