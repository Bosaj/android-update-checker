package ma.bosaj.updateradar.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ma.bosaj.updateradar.MainActivity
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.RadarRepository
import ma.bosaj.updateradar.data.RadarSettings
import java.util.concurrent.TimeUnit

object Reminders {
    const val CHANNEL = "reminders"
    private const val WORK = "update-radar-reminder"
    private const val NOTIFICATION_ID = 42

    fun createChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.channel_desc)
            },
        )
    }

    /** Runs only on an unmetered network (Wi-Fi), optionally only while charging. */
    fun constraints(s: RadarSettings): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresCharging(s.onlyWhileCharging)
        .setRequiresBatteryNotLow(true)
        .build()

    fun schedule(context: Context, s: RadarSettings) {
        val wm = WorkManager.getInstance(context)
        if (!s.remindersOn) {
            wm.cancelUniqueWork(WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(s.reminderEveryDays.toLong(), TimeUnit.DAYS)
            .setConstraints(constraints(s))
            .setInitialDelay(s.reminderEveryDays.toLong(), TimeUnit.DAYS)
            .build()
        wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun notify(context: Context, stale: Int, updated: Int, staleDays: Int) {
        if (!canNotify(context) || (stale == 0 && updated == 0)) return
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val title = context.resources.getQuantityString(R.plurals.notif_stale_title, stale, stale, staleDays)
        val text = if (updated > 0) context.resources.getQuantityString(R.plurals.notif_updated_text, updated, updated)
        else context.getString(R.string.notif_review_text)
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (stale > 0) title else text)
            .setContentText(if (stale > 0) text else context.getString(R.string.notif_review_text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n)
        } catch (_: SecurityException) {
        }
    }
}

/** Periodic scan on Wi-Fi: records updates since the last scan and reminds about stale apps. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = RadarRepository(applicationContext)
        val result = repo.scan()
        val s = repo.settings.current()
        val now = System.currentTimeMillis()
        val summary = Radar.summary(result.apps, now, s.agingAfterDays, s.staleAfterDays, s.ignored)
        Reminders.notify(applicationContext, summary.stale, result.newChanges.size, s.staleAfterDays)
        return Result.success()
    }
}
