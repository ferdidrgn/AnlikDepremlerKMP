package com.ferdidrgn.anlikdepremler.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
class FeltRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Canlı Hissettim Sayısını Dinle
    fun getFeltCount(earthquakeId: String): Flow<Int> = callbackFlow {
        val docRef = db.collection("earthquake_felt").document(earthquakeId)
        val listener = docRef.addSnapshotListener { snapshot, _ ->
            val count = snapshot?.getLong("count")?.toInt() ?: 0
            trySend(count)
        }
        awaitClose { listener.remove() }
    }

    // 2. Hissettim Sayısını Artır
    fun reportFelt(earthquakeId: String) {
        val docRef = db.collection("earthquake_felt").document(earthquakeId)
        docRef.set(
            mapOf("count" to FieldValue.increment(1)),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }
}