package com.ferdidrgn.anlikdepremler.core.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.R
import java.io.File
import java.io.FileOutputStream

/**
 * Shares an earthquake as the rendered image card (EarthquakeShareCardRenderer) plus the
 * existing text template as the share caption, instead of text-only - mirrors the
 * "shareable card" pattern EMSC's LastQuake uses for social virality.
 */
fun shareEarthquakeAsImageCard(context: Context, earthquake: Earthquake) {
    val bitmap = EarthquakeShareCardRenderer.render(earthquake)
    val imageFile = writeToCache(context, bitmap, earthquake.id)
    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )

    val shareText = context.getString(
        R.string.share_template,
        earthquake.location,
        earthquake.region,
        earthquake.magnitude,
        earthquake.depth,
        earthquake.date,
        earthquake.time,
        earthquake.id
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, context.getString(R.string.share_earthquake_info))
    )
}

private fun writeToCache(context: Context, bitmap: Bitmap, earthquakeId: String): File {
    val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(dir, "earthquake_$earthquakeId.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    return file
}
