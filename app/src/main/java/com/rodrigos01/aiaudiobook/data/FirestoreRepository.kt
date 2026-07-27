package com.rodrigos01.aiaudiobook.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class FirestoreRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getTitles(userId: String): Flow<List<Title>> = callbackFlow {
        val ownerIdQuery = "user:$userId"
        val registration = db.collection("titles")
            .whereEqualTo("owner_id", ownerIdQuery)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    val titles = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Title::class.java)?.copy(id = doc.id)
                    }
                    trySend(titles)
                }
            }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    fun getChapters(titleId: String): Flow<List<Chapter>> = callbackFlow {
        val registration = db.collection("chapters")
            .whereEqualTo("title_id", titleId)
            .orderBy("order_index", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    val chapters = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Chapter::class.java)?.copy(id = doc.id)
                    }
                    trySend(chapters)
                }
            }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)
}
