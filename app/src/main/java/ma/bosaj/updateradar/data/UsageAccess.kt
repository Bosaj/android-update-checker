package ma.bosaj.updateradar.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Optional "last used" dates from Android's usage statistics. They need the special "Usage access"
 * that only the user can grant, in Android settings. The dates never leave the phone.
 */
object UsageAccess {
    /** How far back to look. Android keeps yearly usage buckets for about two years. */
    private const val LOOKBACK_DAYS = 730L

    fun granted(context: Context): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION") ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        // "Default" means the decision falls back to the permission itself.
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    /** Package name → epoch millis of the last time the app was used. Empty without Usage access. */
    fun lastUsed(context: Context, now: Long = System.currentTimeMillis()): Map<String, Long> {
        if (!granted(context)) return emptyMap()
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return emptyMap()
        return try {
            usm.queryAndAggregateUsageStats(now - LOOKBACK_DAYS * Radar.DAY, now)
                .mapValues { it.value.lastTimeUsed }
                .filterValues { it > 0 }
        } catch (_: SecurityException) {
            emptyMap()
        }
    }

    /** Android's Usage access screen, opened on this app where the system supports it. */
    fun settingsIntents(context: Context): List<Intent> = listOf(
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, "package:${context.packageName}".toUri()),
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
    ).map { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
}
