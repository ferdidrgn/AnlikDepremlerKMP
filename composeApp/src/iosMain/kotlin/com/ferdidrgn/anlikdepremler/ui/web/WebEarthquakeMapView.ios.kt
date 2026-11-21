package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ferdi.deprem.model.Earthquake

@Composable
actual fun WebEarthquakeMapView(
    earthquakes: List<Earthquake>,
    modifier: Modifier,
    @Suppress("UNUSED_PARAMETER") onEarthquakeClick: (Earthquake) -> Unit
) {
    EarthquakeMapCanvas(earthquakes = earthquakes, modifier = modifier)
}
