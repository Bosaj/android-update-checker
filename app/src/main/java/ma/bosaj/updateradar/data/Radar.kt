package ma.bosaj.updateradar.data

import java.util.concurrent.TimeUnit

/** Pure logic: staleness, sorting, filtering and scan diffs. No Android types, so it is unit-tested. */
object Radar {
    const val DAY = 86_400_000L

    fun daysSince(epochMillis: Long, now: Long): Long =
        TimeUnit.MILLISECONDS.toDays((now - epochMillis).coerceAtLeast(0))

    /**
     * FRESH: updated within the last [agingAfterDays] days.
     * STALE: not updated for [staleAfterDays] days or more.
     * AGING: in between.
     */
    fun freshness(app: InstalledApp, now: Long, agingAfterDays: Int, staleAfterDays: Int): Freshness {
        val d = daysSince(app.lastUpdate, now)
        return when {
            d >= staleAfterDays -> Freshness.STALE
            d >= agingAfterDays -> Freshness.AGING
            else -> Freshness.FRESH
        }
    }

    enum class Sort { STALEST, RECENT, NAME }

    data class Filter(
        val query: String = "",
        val store: Store? = null,
        val onlyStale: Boolean = false,
    )

    fun apply(
        apps: List<InstalledApp>,
        filter: Filter,
        sort: Sort,
        now: Long,
        agingAfterDays: Int,
        staleAfterDays: Int,
        ignored: Set<String>,
    ): List<InstalledApp> {
        val q = filter.query.trim().lowercase()
        return apps.asSequence()
            .filter { it.packageName !in ignored }
            .filter { filter.store == null || it.store == filter.store }
            .filter { q.isEmpty() || it.label.lowercase().contains(q) || it.packageName.contains(q) }
            .filter { !filter.onlyStale || freshness(it, now, agingAfterDays, staleAfterDays) == Freshness.STALE }
            .sortedWith(
                when (sort) {
                    Sort.STALEST -> compareBy<InstalledApp> { it.lastUpdate }.thenBy { it.label.lowercase() }
                    Sort.RECENT -> compareByDescending<InstalledApp> { it.lastUpdate }.thenBy { it.label.lowercase() }
                    Sort.NAME -> compareBy { it.label.lowercase() }
                },
            )
            .toList()
    }

    data class Summary(val total: Int, val fresh: Int, val aging: Int, val stale: Int, val byStore: Map<Store, Int>)

    fun summary(apps: List<InstalledApp>, now: Long, agingAfterDays: Int, staleAfterDays: Int, ignored: Set<String>): Summary {
        val visible = apps.filter { it.packageName !in ignored }
        val counts = visible.groupingBy { freshness(it, now, agingAfterDays, staleAfterDays) }.eachCount()
        return Summary(
            total = visible.size,
            fresh = counts[Freshness.FRESH] ?: 0,
            aging = counts[Freshness.AGING] ?: 0,
            stale = counts[Freshness.STALE] ?: 0,
            byStore = visible.groupingBy { it.store }.eachCount(),
        )
    }

    /** Apps whose version code changed between [before] and [after]; new installs are not counted as updates. */
    fun changes(before: Map<String, Pair<Long, String>>, after: List<InstalledApp>, now: Long): List<AppChange> =
        after.mapNotNull { app ->
            val (oldCode, oldName) = before[app.packageName] ?: return@mapNotNull null
            if (oldCode == app.versionCode) null
            else AppChange(app.packageName, app.label, oldName, app.versionName, if (app.lastUpdate > 0) app.lastUpdate else now)
        }.sortedByDescending { it.at }
}
