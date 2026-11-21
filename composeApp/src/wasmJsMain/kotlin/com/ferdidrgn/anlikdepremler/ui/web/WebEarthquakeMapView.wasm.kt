package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ferdi.deprem.model.Earthquake
import kotlinx.browser.document
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

private var nextMapId = 0

@Composable
actual fun WebEarthquakeMapView(
    earthquakes: List<Earthquake>,
    modifier: Modifier,
    onEarthquakeClick: (Earthquake) -> Unit
) {
    val host = remember { document.createElement("div") as HTMLElement }
    val mapId = remember { "earthquake-map-${nextMapId++}" }
    val density = LocalDensity.current.density
    val currentEarthquakes = rememberUpdatedState(earthquakes)
    val currentOnEarthquakeClick = rememberUpdatedState(onEarthquakeClick)
    val mapData = remember(earthquakes) { earthquakes.toMapGeoJson() }

    DisposableEffect(host, mapId) {
        host.className = "earthquake-map-host"
        host.setAttribute("role", "region")
        host.setAttribute("aria-label", "Etkileşimli deprem haritası")
        val parent = document.getElementById("compose-app") as? HTMLElement
            ?: error("#compose-app container is missing")
        parent.appendChild(host)

        host.addEventListener("quake-marker-select") { event ->
            val earthquakeId = earthquakeIdFromEvent(event)
            val earthquake = earthquakeId?.let { id -> currentEarthquakes.value.firstOrNull { it.id == id } }
            if (earthquake != null) currentOnEarthquakeClick.value(earthquake)
        }
        mountEarthquakeMap(host, mapId, mapData)

        onDispose {
            unmountEarthquakeMap(mapId)
            host.remove()
        }
    }

    LaunchedEffect(mapId, mapData) {
        updateEarthquakeMap(mapId, mapData)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val width = coordinates.size.width / density
                val height = coordinates.size.height / density
                positionEarthquakeMap(
                    mapId = mapId,
                    left = "${position.x / density}px",
                    top = "${position.y / density}px",
                    width = "${width}px",
                    height = "${height}px"
                )
            }
    )
}

private fun List<Earthquake>.toMapGeoJson(): String = buildJsonObject {
    put("type", "FeatureCollection")
    put("features", buildJsonArray {
        filter { it.latitude.isFinite() && it.longitude.isFinite() && it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
            .forEach { earthquake ->
                add(buildJsonObject {
                    put("type", "Feature")
                    put("geometry", buildJsonObject {
                        put("type", "Point")
                        put("coordinates", buildJsonArray {
                            add(JsonPrimitive(earthquake.longitude))
                            add(JsonPrimitive(earthquake.latitude))
                        })
                    })
                    put("properties", buildJsonObject {
                        put("id", earthquake.id)
                        put("location", earthquake.location)
                        put("region", earthquake.region)
                        put("magnitude", JsonPrimitive(earthquake.magnitude.takeIf(Double::isFinite) ?: 0.0))
                        put("depth", JsonPrimitive(earthquake.depth.takeIf(Double::isFinite) ?: 0.0))
                        put("time", "${earthquake.date} ${earthquake.time}")
                        put("source", earthquake.source)
                    })
                })
            }
    })
}.toString()

@OptIn(ExperimentalWasmJsInterop::class)
private fun mountEarthquakeMap(host: HTMLElement, mapId: String, data: String): Unit =
    js("window.EarthquakeMapRuntime.mount(host, mapId, data)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun updateEarthquakeMap(mapId: String, data: String): Unit =
    js("window.EarthquakeMapRuntime.update(mapId, data)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun unmountEarthquakeMap(mapId: String): Unit =
    js("window.EarthquakeMapRuntime.unmount(mapId)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun positionEarthquakeMap(mapId: String, left: String, top: String, width: String, height: String): Unit = js(
    "window.EarthquakeMapRuntime.position(mapId, left, top, width, height)"
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun earthquakeIdFromEvent(event: Event): String? = js("event.detail")
