package com.ferdidrgn.anlikdepremler.core.util

import kotlinx.serialization.Serializable

/** A user-named point (home/work/family) checked for nearby earthquakes alongside the device's
 *  live GPS location - stored as JSON in PreferencesManager since DataStore has no native list-
 *  of-objects support. */
@Serializable
data class SavedLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
