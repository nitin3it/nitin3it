package com.nitin3it.kidsafe.usage

import android.content.Context
import com.nitin3it.kidsafe.data.ProfileRepository
import com.nitin3it.kidsafe.data.UsageRepository
import com.nitin3it.kidsafe.data.model.DailyUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

/** Collects recent usage on the child's phone and uploads it. */
class UsageSyncer(
    context: Context,
    private val collector: UsageCollector = UsageCollector(context),
    private val usageRepository: UsageRepository = UsageRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
) {
    /** Yesterday and today, so late-evening usage is complete after midnight. */
    suspend fun collect(): List<DailyUsage> = withContext(Dispatchers.Default) {
        val today = LocalDate.now()
        listOf(today.minusDays(1), today).map(collector::collectDay)
    }

    /**
     * Firestore queues writes while offline and only completes them once the server confirms,
     * so a timeout here means "saved locally, will upload later" rather than data loss.
     */
    suspend fun upload(childId: String, days: List<DailyUsage>) {
        withTimeout(UPLOAD_TIMEOUT_MS) {
            days.forEach { usageRepository.upload(childId, it) }
            profileRepository.markSynced(childId)
        }
    }

    private companion object {
        const val UPLOAD_TIMEOUT_MS = 20_000L
    }
}
