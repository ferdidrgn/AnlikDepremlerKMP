package com.ferdidrgn.anlikdepremler.core.data

import com.google.firebase.firestore.FieldValue
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

    suspend fun submitFeltReport(earthquakeId: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            firestore.collection(COLLECTION)
                .document(earthquakeId)
                .set(mapOf(FIELD_COUNT to FieldValue.increment(1)), SetOptions.merge())
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(Unit) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
    }

    private companion object {
        const val COLLECTION = "felt_reports"
        const val FIELD_COUNT = "count"
    }
}
