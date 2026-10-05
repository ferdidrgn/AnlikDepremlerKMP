package com.ferdidrgn.anlikdepremler.core.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

/**
 * Home-screen widget showing the most recent earthquake. Deliberately self-contained: it fetches
 * directly from EarthquakeRepository (via Koin's global context - the Application's startKoin()
 * already covers this process) rather than reading MainViewModel state, since a widget's host
 * process/update cycle is independent of whether the app UI is even open.
 */
class EarthquakeWidget : GlanceAppWidget(), KoinComponent {

    private val repository: EarthquakeRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val latestEarthquake = runCatching {
            repository.getEarthquakes(EarthquakeSource.KANDILLI).first().firstOrNull()
        }.getOrNull()

        provideContent {
            WidgetContent(latestEarthquake)
        }
    }
}

/** The widget always renders on its own fixed dark background regardless of system theme, so
 *  every color is "fixed" - day and night are just the same value. */
private fun fixedColor(color: Color) = ColorProvider(day = color, night = color)

@Composable
private fun WidgetContent(earthquake: Earthquake?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (earthquake == null) {
            Text(
                text = "Veri yok",
                style = TextStyle(color = fixedColor(Color.White), fontSize = 13.sp)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", earthquake.magnitude)} Mw",
                    style = TextStyle(
                        color = fixedColor(Color(magnitudeHeatColor(earthquake.magnitude).toArgb())),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = earthquake.location,
                    style = TextStyle(color = fixedColor(Color.White), fontSize = 13.sp)
                )
                Text(
                    text = "${earthquake.date} ${earthquake.time}",
                    style = TextStyle(color = fixedColor(Color(0xFF94A3B8)), fontSize = 11.sp)
                )
            }
        }
    }
}

class EarthquakeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EarthquakeWidget()
}
