package ma.bosaj.updateradar.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One scan of the device: the apps, the updates detected since the previous scan, and whether this is the first scan. */
data class ScanResult(val apps: List<InstalledApp>, val newChanges: List<AppChange>, val firstScan: Boolean)

class RadarRepository(context: Context) {
    private val app = context.applicationContext
    val scanner = AppScanner(app)
    val settings = SettingsRepo(app)
    private val snapshot = Snapshot(app.filesDir)

    suspend fun scan(): ScanResult = withContext(Dispatchers.IO) {
        val s = settings.current()
        val now = System.currentTimeMillis()
        val used = UsageAccess.lastUsed(app, now)
        val apps = scanner.scan(s.includeSystem).map { a -> used[a.packageName]?.let { a.copy(lastUsed = it) } ?: a }
        val firstScan = !snapshot.hasBaseline()
        val changes = if (firstScan) emptyList() else Radar.changes(snapshot.lastScan(), apps, now)
        snapshot.addToFeed(changes)
        snapshot.saveScan(apps)
        settings.setLastScan(now)
        ScanResult(apps, changes, firstScan)
    }

    fun feed(): List<AppChange> = snapshot.feed()

    fun clearFeed() = snapshot.clearFeed()

    fun galaxyStoreInstalled(): Boolean = scanner.isInstalled(Store.GALAXY.packageName!!)
}
