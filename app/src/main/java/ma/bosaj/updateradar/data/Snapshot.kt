package ma.bosaj.updateradar.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * The previous scan (package -> version) and the feed of detected updates, stored as JSON in the app's private files.
 * Detecting updates by comparing scans works on every Android version, without broadcast receivers.
 */
class Snapshot(private val dir: File) {
    private val scanFile get() = File(dir, "last_scan.json")
    private val feedFile get() = File(dir, "updates_feed.json")

    fun lastScan(): Map<String, Pair<Long, String>> = runCatching {
        if (!scanFile.exists()) return emptyMap()
        val o = JSONObject(scanFile.readText())
        o.keys().asSequence().associateWith { k ->
            val v = o.getJSONObject(k)
            v.getLong("code") to v.getString("name")
        }
    }.getOrDefault(emptyMap())

    fun hasBaseline(): Boolean = scanFile.exists()

    fun saveScan(apps: List<InstalledApp>) {
        val o = JSONObject()
        for (a in apps) o.put(a.packageName, JSONObject().put("code", a.versionCode).put("name", a.versionName))
        write(scanFile, o.toString())
    }

    fun feed(): List<AppChange> = runCatching {
        if (!feedFile.exists()) return emptyList()
        val arr = JSONArray(feedFile.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            AppChange(o.getString("pkg"), o.getString("label"), o.getString("from"), o.getString("to"), o.getLong("at"))
        }
    }.getOrDefault(emptyList())

    /** Prepend new changes and keep the feed bounded. */
    fun addToFeed(changes: List<AppChange>, keep: Int = 200) {
        if (changes.isEmpty()) return
        val merged = (changes + feed()).distinctBy { it.packageName + "@" + it.toVersion }.sortedByDescending { it.at }.take(keep)
        val arr = JSONArray()
        for (c in merged) arr.put(JSONObject().put("pkg", c.packageName).put("label", c.label).put("from", c.fromVersion).put("to", c.toVersion).put("at", c.at))
        write(feedFile, arr.toString())
    }

    fun clearFeed() {
        feedFile.delete()
    }

    private fun write(f: File, text: String) {
        dir.mkdirs()
        val tmp = File(dir, f.name + ".tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(f)) {
            f.writeText(text)
            tmp.delete()
        }
    }
}
