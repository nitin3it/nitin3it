package com.nitin3it.kidsafe.data

import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.nitin3it.kidsafe.data.model.Role
import com.nitin3it.kidsafe.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class ProfileRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val profiles = db.collection("profiles")

    fun profileId(uid: String, role: Role) = "${uid}_${role.key}"

    suspend fun getOrCreate(user: FirebaseUser, role: Role): UserProfile {
        val ref = profiles.document(profileId(user.uid, role))
        val existing = ref.get().await()
        existing.toObject<UserProfile>()?.let { return it.copy(id = existing.id) }

        val profile = UserProfile(
            id = ref.id,
            ownerUid = user.uid,
            publicId = generateUniquePublicId(),
            role = role.key,
            displayName = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.phoneNumber ?: user.email ?: "KidSafe user",
            contact = user.email ?: user.phoneNumber ?: "",
            photoUrl = user.photoUrl?.toString(),
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            createdAt = Timestamp.now(),
        )
        ref.set(profile).await()
        return profile
    }

    private suspend fun generateUniquePublicId(): String {
        repeat(5) {
            val candidate = PublicIds.generate()
            val clash = profiles.whereEqualTo("publicId", candidate).limit(1).get().await()
            if (clash.isEmpty) return candidate
        }
        error("Could not generate a unique ID, please try again")
    }

    suspend fun findChildByPublicId(code: String): UserProfile? {
        val result = profiles.whereEqualTo("publicId", PublicIds.normalize(code)).get().await()
        return result.documents
            .mapNotNull { doc -> doc.toObject<UserProfile>()?.copy(id = doc.id) }
            .firstOrNull { it.role == Role.CHILD.key }
    }

    suspend fun linkChild(parentId: String, childId: String) {
        profiles.document(parentId).update("linkedChildren", FieldValue.arrayUnion(childId)).await()
    }

    suspend fun unlinkChild(parentId: String, childId: String) {
        profiles.document(parentId).update("linkedChildren", FieldValue.arrayRemove(childId)).await()
    }

    suspend fun markSynced(childId: String) {
        profiles.document(childId).update(
            mapOf(
                "lastSyncAt" to FieldValue.serverTimestamp(),
                "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
            )
        ).await()
    }

    fun observe(profileId: String): Flow<UserProfile?> = callbackFlow {
        val registration = profiles.document(profileId).addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snap?.toObject<UserProfile>()?.copy(id = snap.id))
        }
        awaitClose { registration.remove() }
    }

    fun observeAll(profileIds: List<String>): Flow<List<UserProfile>> {
        if (profileIds.isEmpty()) return flowOf(emptyList())
        return combine(profileIds.map(::observe)) { it.filterNotNull() }
    }
}
