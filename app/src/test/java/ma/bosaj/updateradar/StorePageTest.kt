package ma.bosaj.updateradar

import ma.bosaj.updateradar.data.InstalledApp
import ma.bosaj.updateradar.data.Freshness
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.helper.PageNode
import ma.bosaj.updateradar.helper.PageState
import ma.bosaj.updateradar.helper.StorePage
import org.junit.Assert.assertEquals
import org.junit.Test

/** Screens captured with uiautomator on a Galaxy S23+ (Play Store and Galaxy Store, English UI, Oct 2026). */
class StorePageTest {
    private fun nodes(vararg texts: String) = texts.map { PageNode(it) }

    private val playUpdatesOverview = nodes(
        "No harmful apps found", "Play Protect scanned at 05:32", "Updates available", "18 updates pending",
        "Update all", "See details", "195 GB of 238 GB used", "Manage apps & device", "Overview", "Manage",
    )
    private val playAppCurrent = nodes("YouTube", "Google LLC", "Contains ads", "In-app purchases", "Uninstall", "Open", "Games", "Apps", "Search", "You")
    private val playAppOutdated = nodes("YouTube", "Google LLC", "Uninstall", "Update", "What's new", "Updated on Oct 2, 2026")
    private val galaxyAppCurrent = listOf(
        PageNode("Samsung TTS UK English Voice 1", "${StorePage.GALAXY}:id/tv_detail_main_product_name"),
        PageNode("Uninstall", "${StorePage.GALAXY}:id/tv_detail_uninstall"),
        PageNode("Installed", StorePage.GALAXY_MAIN_BUTTON),
    )
    private val galaxyAppOutdated = galaxyAppCurrent.dropLast(1) + PageNode("Update", StorePage.GALAXY_MAIN_BUTTON)

    @Test fun findsPlayUpdateAll() {
        assertEquals(PageState.UPDATE_ALL, StorePage.classify(playUpdatesOverview, wantUpdateAll = true))
        assertEquals(4, StorePage.target(playUpdatesOverview, PageState.UPDATE_ALL))
    }

    @Test fun updatesOverviewWithoutUpdateAllKeepsWaiting() {
        val noUpdates = playUpdatesOverview.filterNot { it.text == "Update all" || it.text == "18 updates pending" }
        assertEquals(PageState.LOADING, StorePage.classify(noUpdates, wantUpdateAll = true))
    }

    @Test fun playAppPages() {
        assertEquals(PageState.CURRENT, StorePage.classify(playAppCurrent, wantUpdateAll = false))
        assertEquals(PageState.UPDATE, StorePage.classify(playAppOutdated, wantUpdateAll = false))
        // "Updated on …" must not be mistaken for the button.
        assertEquals(3, StorePage.target(playAppOutdated, PageState.UPDATE))
    }

    @Test fun galaxyAppPagesUseTheViewId() {
        assertEquals(PageState.CURRENT, StorePage.classify(galaxyAppCurrent, wantUpdateAll = false))
        assertEquals(PageState.UPDATE, StorePage.classify(galaxyAppOutdated, wantUpdateAll = false))
        assertEquals(2, StorePage.target(galaxyAppOutdated, PageState.UPDATE))
    }

    @Test fun loadingPageIsNotJudged() {
        assertEquals(PageState.LOADING, StorePage.classify(nodes("YouTube", "Google LLC"), wantUpdateAll = false))
    }

    @Test fun otherLanguagesAndMarks() {
        assertEquals(PageState.UPDATE, StorePage.classify(nodes("Désinstaller", "Mettre à jour"), wantUpdateAll = false))
        assertEquals(PageState.UPDATE_ALL, StorePage.classify(nodes("تحديث الكل"), wantUpdateAll = true))
        assertEquals(PageState.CURRENT, StorePage.classify(nodes("مثبّت"), wantUpdateAll = false))
        assertEquals(PageState.CURRENT, StorePage.classify(nodes("  ÖFFNEN "), wantUpdateAll = false))
    }

    @Test fun storeCheckMakesAnOldAppFresh() {
        val now = 1_800_000_000_000L
        val old = InstalledApp("a", "A", "1", 1, now - 300 * Radar.DAY, now - 400 * Radar.DAY, Store.PLAY, Store.PLAY.packageName, false)
        assertEquals(Freshness.STALE, Radar.freshness(old, now, 14, 45))
        assertEquals(Freshness.FRESH, Radar.freshness(old.copy(verifiedAt = now - 2 * Radar.DAY), now, 14, 45))
        // A check older than VERIFIED_DAYS no longer counts.
        assertEquals(Freshness.STALE, Radar.freshness(old.copy(verifiedAt = now - 31 * Radar.DAY), now, 14, 45))
    }
}
