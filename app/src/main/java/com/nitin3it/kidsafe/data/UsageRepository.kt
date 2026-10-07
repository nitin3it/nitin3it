package com.nitin3it.kidsafe.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.nitin3it.kidsafe.data.model.DailyUsage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class UsageRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private fun usage(childId: String) =
        db.collection("profiles").document(childId).collection("usage")

    suspend fun upload(childId: String, day: DailyUsage) {
        val data = mapOf(
            "date" to day.date,
            "totalMs" to day.totalMs,
            "apps" to day.apps,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        usage(childId).document(day.date).set(data).await()
    }

    /** Live usage for the last [days] days (including today), oldest first. */
    fun observeRecent(childId: String, days: Long = 7): Flow<List<DailyUsage>> = callbackFlow {
        val from = LocalDate.now().minusDays(days - 1).toString()
        val registration = usage(childId)
            .whereGreaterThanOrEqualTo("date", from)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty()
                    .mapNotNull { it.toObject<DailyUsage>() }
                    .sortedBy { it.date }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }
}
