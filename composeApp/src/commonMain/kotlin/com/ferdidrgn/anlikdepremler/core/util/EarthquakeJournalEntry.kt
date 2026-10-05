package com.ferdidrgn.anlikdepremler.core.util

import kotlinx.serialization.Serializable

/** A snapshot of an earthquake's key fields at the moment the user marked "I felt it" - stored
 *  by value (not just the id) since the earthquake might drop out of the live fetched list
 *  later (filtered by time span, or from a different source) and still needs to render here. */
@Serializable
data class EarthquakeJournalEntry(
    val earthquakeId: String,
    val location: String,
    val region: String,
    val magnitude: Double,
    val date: String,
    val time: String,
    val markedAtMillis: Long
)
