package com.nitin3it.kidsafe.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.nitin3it.kidsafe.data.model.AppUsage
import com.nitin3it.kidsafe.data.model.DailyUsage
import java.time.LocalDate
import java.time.ZoneId

/** Reads per-app foreground time on this device from [UsageStatsManager]. */
class UsageCollector(private val context: Context) {

    private val usageStats = context.getSystemService(UsageStatsManager::class.java)
    private val pm = context.packageManager

    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun collectDay(date: LocalDate): DailyUsage {
        val zone = ZoneId.systemDefault()
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = minOf(date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), System.currentTimeMillis())

        val totals = HashMap<String, Long>()
        val launches = HashMap<String, Int>()
        val lastUsed = HashMap<String, Long>()
        val openSince = HashMap<String, Long>()
        var foregroundPackage: String? = null

        fun close(pkg: String, at: Long) {
            val start = openSince.remove(pkg) ?: return
            val from = maxOf(start, dayStart)
            val to = minOf(at, dayEnd)
            if (to > from) totals[pkg] = (totals[pkg] ?: 0L) + (to - from)
            lastUsed[pkg] = maxOf(lastUsed[pkg] ?: 0L, to)
        }

        // Look back a little so an app already open at midnight is counted from midnight.
        val events = usageStats.queryEvents(dayStart - LOOKBACK_MS, dayEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val ts = event.timeStamp
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (pkg !in openSince) openSince[pkg] = ts
                    if (pkg != foregroundPackage && ts >= dayStart) {
                        launches[pkg] = (launches[pkg] ?: 0) + 1
                    }
                    foregroundPackage = pkg
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> close(pkg, ts)
                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.DEVICE_SHUTDOWN -> {
                    openSince.keys.toList().forEach { close(it, ts) }
                    foregroundPackage = null
                }
            }
        }
        openSince.keys.toList().forEach { close(it, dayEnd) }

        val hidden = launcherPackages() + context.packageName
        val apps = totals
            .filter { (pkg, ms) -> ms >= MIN_USAGE_MS && pkg !in hidden && isUserFacing(pkg) }
            .map { (pkg, ms) ->
                AppUsage(
                    packageName = pkg,
                    appName = appLabel(pkg),
                    totalMs = ms,
                    launches = launches[pkg] ?: 0,
                    lastUsed = lastUsed[pkg] ?: 0L,
                )
            }
            .sortedByDescending { it.totalMs }

        return DailyUsage(date = date.toString(), totalMs = apps.sumOf { it.totalMs }, apps = apps)
    }

    private fun isUserFacing(pkg: String) = pm.getLaunchIntentForPackage(pkg) != null

    private fun launcherPackages(): Set<String> {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private fun appLabel(pkg: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        pkg
    }

    private companion object {
        const val LOOKBACK_MS = 3 * 60 * 60 * 1000L
        const val MIN_USAGE_MS = 1_000L
    }
}
