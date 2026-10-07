package com.nitin3it.kidsafe.usage

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.nitin3it.kidsafe.KidSafeApp
import com.nitin3it.kidsafe.data.ProfileRepository
import com.nitin3it.kidsafe.data.RolePreferences
import com.nitin3it.kidsafe.data.model.Role
import java.util.concurrent.TimeUnit

/** Uploads the child's app usage every 15 minutes (the shortest period WorkManager allows). */
class UsageSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!KidSafeApp.isFirebaseConfigured(applicationContext)) return Result.success()
        if (RolePreferences(applicationContext).role != Role.CHILD) return Result.success()
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        if (!UsageCollector(applicationContext).hasPermission()) return Result.success()

        val childId = ProfileRepository().profileId(user.uid, Role.CHILD)
        return try {
            val syncer = UsageSyncer(applicationContext)
            syncer.upload(childId, syncer.collect())
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "usage-sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UsageSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
