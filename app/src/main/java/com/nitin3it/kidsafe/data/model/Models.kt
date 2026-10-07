package com.nitin3it.kidsafe.data.model

import com.google.firebase.Timestamp

enum class Role(val key: String) {
    CHILD("child"),
    PARENT("parent");

    companion object {
        fun from(key: String?): Role? = entries.firstOrNull { it.key == key }
    }
}

// Properties are `var` because Firestore deserializes through setters.

/**
 * One profile per (account, role). Stored at `profiles/{ownerUid}_{role}`, so the same
 * Google account or phone number gets two different IDs when used as a child and as a parent.
 */
data class UserProfile(
    var id: String = "",
    var ownerUid: String = "",
    /** Short, human-friendly code (e.g. `K7QM-29TX`) that a child shares with a parent. */
    var publicId: String = "",
    var role: String = "",
    var displayName: String = "",
    var contact: String = "",
    var photoUrl: String? = null,
    var deviceModel: String? = null,
    /** Parent only: profile IDs of the linked children. */
    var linkedChildren: List<String> = emptyList(),
    /** Child only: when usage was last uploaded. */
    var lastSyncAt: Timestamp? = null,
    var createdAt: Timestamp? = null,
)

data class AppUsage(
    var packageName: String = "",
    var appName: String = "",
    var totalMs: Long = 0,
    var launches: Int = 0,
    var lastUsed: Long = 0,
)

/** Usage for one calendar day, stored at `profiles/{childId}/usage/{yyyy-MM-dd}`. */
data class DailyUsage(
    var date: String = "",
    var totalMs: Long = 0,
    var apps: List<AppUsage> = emptyList(),
    var updatedAt: Timestamp? = null,
)
