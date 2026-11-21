package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ferdi.deprem.model.Earthquake

/** Platform map surface used by the web dashboard. */
@Composable
expect fun WebEarthquakeMapView(
    earthquakes: List<Earthquake>,
    modifier: Modifier = Modifier,
    onEarthquakeClick: (Earthquake) -> Unit
)
