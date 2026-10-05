package com.ferdidrgn.anlikdepremler.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class EarthquakeComment(
    val id: String,
    val text: String,
    val timestampMillis: Long,
    val reported: Boolean = false
)

/**
 * Short eyewitness comments per earthquake, stored as a Firestore subcollection under the same
 * felt_reports/{earthquakeId} doc FeltReportRepository already writes to. There's no backend
 * here to moderate content automatically, so reportComment() only flags a doc for the app owner
 * to review by hand in the Firebase Console - it doesn't hide it client-side, since otherwise a
 * bad-faith reporter could mass-hide other people's comments for everyone.
 */
class EarthquakeCommentRepository {

    private val firestore get() = FirebaseFirestore.getInstance()

    fun observeComments(earthquakeId: String): Flow<List<EarthquakeComment>> = callbackFlow {
        val registration = firestore.collection(COLLECTION)
            .document(earthquakeId)
            .collection(SUBCOLLECTION)
            .orderBy("timestampMillis", Query.Direction.DESCENDING)
            .limit(MAX_COMMENTS.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    EarthquakeComment(
                        id = doc.id,
                        text = text,
                        timestampMillis = doc.getLong("timestampMillis") ?: 0L,
                        reported = doc.getBoolean("reported") ?: false
                    )
                }.orEmpty()
                trySend(comments)
            }
        awaitClose { registration.remove() }
    }

    suspend fun submitComment(earthquakeId: String, text: String) {
        val data = mapOf(
            "text" to text.take(MAX_COMMENT_LENGTH),
            "timestampMillis" to System.currentTimeMillis(),
            "reported" to false
        )
        suspendCancellableCoroutine<Unit> { continuation ->
            firestore.collection(COLLECTION)
                .document(earthquakeId)
                .collection(SUBCOLLECTION)
                .add(data)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(Unit) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
    }

    suspend fun reportComment(earthquakeId: String, commentId: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            firestore.collection(COLLECTION)
                .document(earthquakeId)
                .collection(SUBCOLLECTION)
                .document(commentId)
                .update("reported", true)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(Unit) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
    }

    companion object {
        private const val COLLECTION = "felt_reports"
        private const val SUBCOLLECTION = "comments"
        const val MAX_COMMENT_LENGTH = 300
        private const val MAX_COMMENTS = 30
    }
}
