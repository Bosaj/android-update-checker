package ma.bosaj.updateradar

import ma.bosaj.updateradar.data.AppChange
import ma.bosaj.updateradar.data.Freshness
import ma.bosaj.updateradar.data.InstalledApp
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.Snapshot
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.util.StoreLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class RadarTest {
    private val now = 1_800_000_000_000L
    private fun app(pkg: String, daysAgo: Int, store: Store = Store.PLAY, code: Long = 1, label: String = pkg) =
        InstalledApp(pkg, label, "1.$code", code, now - daysAgo * Radar.DAY, now - 400 * Radar.DAY, store, store.packageName, false)

    @Test fun freshnessUsesBothThresholds() {
        assertEquals(Freshness.FRESH, Radar.freshness(app("a", 0), now, 14, 45))
        assertEquals(Freshness.FRESH, Radar.freshness(app("a", 13), now, 14, 45))
        assertEquals(Freshness.AGING, Radar.freshness(app("a", 14), now, 14, 45))
        assertEquals(Freshness.AGING, Radar.freshness(app("a", 44), now, 14, 45))
        assertEquals(Freshness.STALE, Radar.freshness(app("a", 45), now, 14, 45))
    }

    @Test fun romPlaceholderDatesAreFactoryVersions() {
        assertTrue(Radar.isFactoryDate(0))
        assertTrue(Radar.isFactoryDate(1_230_768_000_000L)) // 2009-01-01, common ROM stamp
        assertFalse(Radar.isFactoryDate(now - 3 * Radar.DAY))
        val factory = app("com.vendor", 0).copy(lastUpdate = 1_230_768_000_000L)
        assertEquals(Freshness.STALE, Radar.freshness(factory, now, 14, 45))
    }

    @Test fun futureTimestampsCountAsToday() {
        assertEquals(0, Radar.daysSince(now + Radar.DAY, now))
    }

    @Test fun filtersSortsAndIgnores() {
        val apps = listOf(app("com.b", 90, label = "Bravo"), app("com.a", 3, Store.GALAXY, label = "Alpha"), app("com.c", 60, Store.SIDELOADED, label = "Charlie"))
        val stalest = Radar.apply(apps, Radar.Filter(), Radar.Sort.STALEST, now, 14, 45, emptySet())
        assertEquals(listOf("com.b", "com.c", "com.a"), stalest.map { it.packageName })
        val recent = Radar.apply(apps, Radar.Filter(), Radar.Sort.RECENT, now, 14, 45, emptySet())
        assertEquals("com.a", recent.first().packageName)
        val byName = Radar.apply(apps, Radar.Filter(), Radar.Sort.NAME, now, 14, 45, emptySet())
        assertEquals(listOf("Alpha", "Bravo", "Charlie"), byName.map { it.label })
        val galaxy = Radar.apply(apps, Radar.Filter(store = Store.GALAXY), Radar.Sort.NAME, now, 14, 45, emptySet())
        assertEquals(listOf("com.a"), galaxy.map { it.packageName })
        val staleOnly = Radar.apply(apps, Radar.Filter(onlyStale = true), Radar.Sort.NAME, now, 14, 45, setOf("com.c"))
        assertEquals(listOf("com.b"), staleOnly.map { it.packageName })
        val search = Radar.apply(apps, Radar.Filter(query = "char"), Radar.Sort.NAME, now, 14, 45, emptySet())
        assertEquals(listOf("com.c"), search.map { it.packageName })
    }

    @Test fun summaryCountsVisibleApps() {
        val apps = listOf(app("a", 1), app("b", 20), app("c", 50), app("d", 100, Store.GALAXY))
        val s = Radar.summary(apps, now, 14, 45, setOf("d"))
        assertEquals(3, s.total)
        assertEquals(1, s.fresh)
        assertEquals(1, s.aging)
        assertEquals(1, s.stale)
        assertEquals(mapOf(Store.PLAY to 3), s.byStore)
    }

    @Test fun changesReportVersionBumpsOnly() {
        val before = mapOf("a" to (1L to "1.1"), "b" to (5L to "1.5"))
        val after = listOf(app("a", 0, code = 2), app("b", 10, code = 5), app("new", 0, code = 1))
        val c = Radar.changes(before, after, now)
        assertEquals(listOf("a"), c.map { it.packageName })
        assertEquals("1.1", c.single().fromVersion)
        assertEquals("1.2", c.single().toVersion)
    }

    @Test fun installerMapping() {
        assertEquals(Store.PLAY, Store.fromInstaller("com.android.vending", false))
        assertEquals(Store.GALAXY, Store.fromInstaller("com.sec.android.app.samsungapps", false))
        assertEquals(Store.SIDELOADED, Store.fromInstaller("com.google.android.packageinstaller", false))
        assertEquals(Store.SIDELOADED, Store.fromInstaller(null, false))
        assertEquals(Store.PREINSTALLED, Store.fromInstaller(null, true))
        assertEquals(Store.OTHER_STORE, Store.fromInstaller("com.example.market", false))
    }

    @Test fun storeTargetFallsBackToPlay() {
        assertEquals(Store.GALAXY, StoreLinks.targetStore(Store.GALAXY, galaxyInstalled = true))
        assertEquals(Store.PLAY, StoreLinks.targetStore(Store.GALAXY, galaxyInstalled = false))
        assertEquals(Store.PLAY, StoreLinks.targetStore(Store.SIDELOADED, galaxyInstalled = true))
    }

    @Test fun samsungPreinstalledAppsGoToGalaxyStore() {
        assertEquals(Store.GALAXY, StoreLinks.targetStore(Store.PREINSTALLED, galaxyInstalled = true, pkg = "com.samsung.android.calendar"))
        assertEquals(Store.GALAXY, StoreLinks.targetStore(Store.PREINSTALLED, galaxyInstalled = true, pkg = "com.sec.android.app.camera"))
        assertEquals(Store.PLAY, StoreLinks.targetStore(Store.PREINSTALLED, galaxyInstalled = true, pkg = "com.google.android.youtube"))
        assertEquals(Store.PLAY, StoreLinks.targetStore(Store.PREINSTALLED, galaxyInstalled = false, pkg = "com.samsung.android.calendar"))
    }

    @Test fun sortsByLastUseThenStalest() {
        val apps = listOf(
            app("never.old", 90), app("never.new", 50),
            app("used.today", 60).copy(lastUsed = now), app("used.week", 70).copy(lastUsed = now - 7 * Radar.DAY),
        )
        val used = Radar.apply(apps, Radar.Filter(), Radar.Sort.USED, now, 14, 45, emptySet())
        assertEquals(listOf("used.today", "used.week", "never.old", "never.new"), used.map { it.packageName })
    }

    @Test fun snapshotRoundTripAndFeed() {
        val dir = Files.createTempDirectory("radar").toFile()
        val snap = Snapshot(dir)
        assertFalse(snap.hasBaseline())
        snap.saveScan(listOf(app("a", 1, code = 7)))
        assertTrue(snap.hasBaseline())
        assertEquals(mapOf("a" to (7L to "1.7")), snap.lastScan())
        snap.addToFeed(listOf(AppChange("a", "A", "1.6", "1.7", now)))
        snap.addToFeed(listOf(AppChange("a", "A", "1.6", "1.7", now), AppChange("b", "B", "2", "3", now + 1)))
        assertEquals(listOf("b", "a"), snap.feed().map { it.packageName })
        snap.clearFeed()
        assertTrue(snap.feed().isEmpty())
        dir.deleteRecursively()
    }
}
