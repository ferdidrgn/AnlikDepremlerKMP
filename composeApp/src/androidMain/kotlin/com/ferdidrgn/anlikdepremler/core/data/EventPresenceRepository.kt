package com.ferdidrgn.anlikdepremler.core.data

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import java.util.UUID

/**
 * Approximates EMSC LastQuake's "lots of people have this page open right now" signal without a
 * backend: each viewer heartbeats its own tiny presence doc every HEARTBEAT_INTERVAL_MILLIS
 * while the detail screen is open, and the live count is "how many of those docs were touched in
 * the last PRESENCE_WINDOW_MILLIS" - computed client-side on read, since there's no Cloud
 * Function to do this server-side. A doc left behind by a killed app ages out of every other
 * client's window immediately (stops being counted), and out of the database itself within
 * VIEWER_DOC_TTL_MILLIS via Firestore's native TTL policy on the "expiresAt" field (configured in
 * the Firebase Console, not in code) - no Cloud Function needed for that either.
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
     *  other viewers' counts a little early, and the next one 15s later fixes it. Also writes
     *  "expiresAt" a few minutes into the future so Firestore's TTL policy (configured in the
     *  Firebase Console) actually deletes the doc if the app is killed mid-session instead of
     *  leaving it behind forever - the client-side PRESENCE_WINDOW_MILLIS filter above already
     *  hides it from live counts, but TTL is what removes the litter from the database itself. */
    fun heartbeat(earthquakeId: String) {
        val expiresAt = Timestamp(Date(System.currentTimeMillis() + VIEWER_DOC_TTL_MILLIS))
        firestore.collection(COLLECTION)
            .document(earthquakeId)
            .collection(SUBCOLLECTION)
            .document(deviceId)
            .set(
                mapOf(
                    "lastSeenMillis" to System.currentTimeMillis(),
                    "expiresAt" to expiresAt
                )
            )
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
        private const val VIEWER_DOC_TTL_MILLIS = 10 * 60_000L
    }
}
