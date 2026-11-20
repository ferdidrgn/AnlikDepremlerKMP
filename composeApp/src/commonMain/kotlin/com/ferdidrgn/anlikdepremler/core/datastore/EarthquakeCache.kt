package com.ferdidrgn.anlikdepremler.core.datastore

import com.ferdi.deprem.model.Earthquake
import kotlinx.serialization.Serializable

/** A small, platform-neutral snapshot used to keep the last successful feed available offline. */
data class EarthquakeCacheSnapshot(
    val earthquakes: List<Earthquake>,
    val cachedAtEpochMillis: Long,
    val sourceName: String
)

@Serializable
internal data class StoredEarthquakeSnapshot(
    val cachedAtEpochMillis: Long,
    val sourceName: String,
    val earthquakes: List<StoredEarthquake>
)

@Serializable
internal data class StoredEarthquake(
    val id: String,
    val location: String,
    val region: String,
    val magnitude: Double,
    val depth: Double,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val cityImageUrl: String,
    val isSignificant: Boolean,
    val intensity: String,
    val source: String
) {
    fun toEarthquake() = Earthquake(
        id = id,
        location = location,
        region = region,
        magnitude = magnitude,
        depth = depth,
        date = date,
        time = time,
        latitude = latitude,
        longitude = longitude,
        cityImageUrl = cityImageUrl,
        isSignificant = isSignificant,
        intensity = intensity,
        source = source
    )

    companion object {
        fun from(earthquake: Earthquake) = StoredEarthquake(
            id = earthquake.id,
            location = earthquake.location,
            region = earthquake.region,
            magnitude = earthquake.magnitude,
            depth = earthquake.depth,
            date = earthquake.date,
            time = earthquake.time,
            latitude = earthquake.latitude,
            longitude = earthquake.longitude,
            cityImageUrl = earthquake.cityImageUrl,
            isSignificant = earthquake.isSignificant,
            intensity = earthquake.intensity,
            source = earthquake.source
        )
    }
}
