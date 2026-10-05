package com.ferdidrgn.anlikdepremler.core.data

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Approximates EMSC LastQuake's "lots of people have this page open right now" signal without a
 * backend: each viewer heartbeats its own tiny presence doc every HEARTBEAT_INTERVAL_MILLIS
 * while the detail screen is open, and the live count is "how many of those docs were touched in
 * the last PRESENCE_WINDOW_MILLIS" - computed client-side on read, since there's no Cloud
 * Function to expire stale docs server-side. A doc left behind by a killed app just ages out of
 * every other client's window and stops being counted; it's harmless litter, not a real bug.
 */
class EventPresenceRepository(private val context: Context) {

    private val firestore get() = FirebaseFirestore.getInstance()
    private val deviceId: String by lazy { getOrCreateDeviceId() }

    fun observeViewerCount(earthquakeId: String): Flow<Int> = callbackFlow {
        val registration = firestore.collection(COLLECTION)
            .document(earthquakeId)
            .collection(SUBCOLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val now = System.currentTimeMillis()
                val activeCount = snapshot?.documents?.count { doc ->
                    val lastSeen = doc.getLong("lastSeenMillis") ?: 0L
                    now - lastSeen <= PRESENCE_WINDOW_MILLIS
                } ?: 0
                trySend(activeCount)
            }
        awaitClose { registration.remove() }
    }

    /** Best-effort, fire-and-forget - a missed heartbeat just means this device ages out of
     *  other viewers' counts a little early, and the next one 15s later fixes it. */
    fun heartbeat(earthquakeId: String) {
        firestore.collection(COLLECTION)
            .document(earthquakeId)
            .collection(SUBCOLLECTION)
            .document(deviceId)
            .set(mapOf("lastSeenMillis" to System.currentTimeMillis()))
    }

    private fun getOrCreateDeviceId(): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("device_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit { putString("device_id", id) }
        }
        return id
    }

    companion object {
        const val HEARTBEAT_INTERVAL_MILLIS = 15_000L
        private const val COLLECTION = "felt_reports"
        private const val SUBCOLLECTION = "viewers"
        private const val PRESENCE_WINDOW_MILLIS = 60_000L
    }
}
