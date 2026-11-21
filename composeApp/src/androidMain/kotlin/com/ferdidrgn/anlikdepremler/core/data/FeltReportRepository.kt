package com.ferdidrgn.anlikdepremler.core.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Replaces EarthquakeDetailScreen's old fake, local-only "felt it" counter with a real
 * crowd-sourced one: a single aggregate doc per earthquake in Firestore's "felt_reports"
 * collection, incremented atomically via FieldValue.increment. Plain client reads/writes within
 * Firestore's free Spark-tier quota - no Cloud Function, no backend of our own. Mirrors the
 * direct-client-write pattern FcmTokenManager already uses against the "devices" collection.
 *
 * Note: this needs Firestore security rules (managed in the Firebase Console, not deployed
 * automatically from this repo) to allow reads/writes on "felt_reports" - see /firestore.rules
 * at the repo root for the exact rules covering this collection plus its comments/viewers
 * subcollections.
 */
class FeltReportRepository {

    private val firestore get() = FirebaseFirestore.getInstance()

    /** Live count that updates in real time as other users report feeling the same earthquake. */
    fun observeFeltCount(earthquakeId: String): Flow<Long> = callbackFlow {
        val registration = firestore.collection(COLLECTION)
            .document(earthquakeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                trySend(snapshot?.getLong(FIELD_COUNT) ?: 0L)
            }
        awaitClose { registration.remove() }
    }

    /** Aggregate intensity bands only; individual reporter documents stay private. */
    fun observeIntensityCounts(earthquakeId: String): Flow<Map<Int, Long>> = callbackFlow {
        val registration = firestore.collection(COLLECTION)
            .document(earthquakeId)
            .collection(INTENSITY_COUNTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val counts = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val level = document.id.removePrefix(INTENSITY_PREFIX).toIntOrNull()
                    val count = document.getLong(FIELD_COUNT)
                    if (level != null && level in 2..12 && count != null && count > 0L) level to count else null
                }.toMap()
                trySend(counts)
            }
        awaitClose { registration.remove() }
    }

    suspend fun submitFeltReport(earthquakeId: String, intensityLevel: Int) {
        require(intensityLevel in 2..12) { "Perceived intensity must be between II and XII" }
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Firebase anonymous authentication is not ready")
        suspendCancellableCoroutine<Unit> { continuation ->
            val reportRef = firestore.collection(COLLECTION).document(earthquakeId)
            val reporterRef = reportRef.collection(REPORTERS).document(userId)
            val intensityRef = reportRef.collection(INTENSITY_COUNTS).document("$INTENSITY_PREFIX$intensityLevel")
            firestore.runTransaction { transaction ->
                if (transaction.get(reporterRef).exists()) return@runTransaction false
                val currentCount = transaction.get(reportRef).getLong(FIELD_COUNT) ?: 0L
                val currentIntensityCount = transaction.get(intensityRef).getLong(FIELD_COUNT) ?: 0L
                transaction.set(
                    reportRef,
                    mapOf(FIELD_COUNT to currentCount + 1L),
                    SetOptions.merge()
                )
                transaction.set(
                    intensityRef,
                    mapOf(FIELD_COUNT to currentIntensityCount + 1L),
                    SetOptions.merge()
                )
                transaction.set(
                    reporterRef,
                    mapOf("createdAt" to Timestamp.now(), FIELD_INTENSITY_BAND to "$INTENSITY_PREFIX$intensityLevel")
                )
                true
            }
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(Unit) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
    }

    private companion object {
        const val COLLECTION = "felt_reports"
        const val FIELD_COUNT = "count"
        const val FIELD_INTENSITY_BAND = "intensityBand"
        const val REPORTERS = "reporters"
        const val INTENSITY_COUNTS = "intensity_counts"
        const val INTENSITY_PREFIX = "mmi_"
    }
}
