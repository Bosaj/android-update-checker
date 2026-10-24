package ma.bosaj.updateradar.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import ma.bosaj.updateradar.data.Store

/**
 * Opens the right store page for an app. A normal app cannot start an update for another app,
 * so the best it can do is take the user to the page where the store's own Update button is.
 */
object StoreLinks {
    /**
     * The store whose page we open: the installer when we know it. Samsung's own preinstalled apps
     * are updated by Galaxy Store, so they go there too; everything else goes to Play Store.
     */
    fun targetStore(store: Store, galaxyInstalled: Boolean, pkg: String = ""): Store = when {
        !galaxyInstalled -> Store.PLAY
        store == Store.GALAXY -> Store.GALAXY
        store == Store.PREINSTALLED && isSamsungApp(pkg) -> Store.GALAXY
        else -> Store.PLAY
    }

    fun isSamsungApp(pkg: String): Boolean = pkg.startsWith("com.samsung.") || pkg.startsWith("com.sec.")

    fun pageIntents(pkg: String, store: Store): List<Intent> = when (store) {
        Store.GALAXY -> listOf(
            Intent(Intent.ACTION_VIEW, "samsungapps://ProductDetail/$pkg".toUri()).setPackage(Store.GALAXY.packageName),
            Intent(Intent.ACTION_VIEW, "https://galaxystore.samsung.com/detail/$pkg".toUri()),
        )
        else -> listOf(
            Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()).setPackage(Store.PLAY.packageName),
            Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()),
            Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$pkg".toUri()),
        )
    }.map { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    /** Try each intent in order; returns false when nothing can handle any of them. */
    fun launchFirst(context: Context, intents: List<Intent>): Boolean {
        for (i in intents) {
            try {
                context.startActivity(i)
                return true
            } catch (_: ActivityNotFoundException) {
            } catch (_: SecurityException) {
            }
        }
        return false
    }

    fun openStorePage(context: Context, pkg: String, store: Store, galaxyInstalled: Boolean): Boolean =
        launchFirst(context, pageIntents(pkg, targetStore(store, galaxyInstalled, pkg)))

    /**
     * Opens the page that lists every pending update, where the store's own "Update all" button is.
     * Opening it also makes the store check for new versions. Play Store has an intent for this page;
     * Galaxy Store has no public link to it (only ProductDetail, SellerDetail, CategoryList, MainPage,
     * ProductSetList and SearchResult), so it opens on its home page.
     */
    fun openUpdatesPage(context: Context, store: Store): Boolean = when (store) {
        Store.PLAY -> launchFirst(
            context,
            listOf(Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS").setPackage(Store.PLAY.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)),
        ) || openStoreHome(context, store)
        else -> openStoreHome(context, store)
    }

    /** Opens a store's own app so the user can tap its Updates tab. */
    fun openStoreHome(context: Context, store: Store): Boolean {
        val pkg = store.packageName ?: return false
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return false
        return launchFirst(context, listOf(launch))
    }

    /** Opens a web page in the browser (the app itself has no internet permission). */
    fun openUrl(context: Context, url: String): Boolean =
        launchFirst(context, listOf(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))

    fun openAppInfo(context: Context, pkg: String): Boolean = launchFirst(
        context,
        listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$pkg".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)),
    )

    fun openApp(context: Context, pkg: String): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return false
        return launchFirst(context, listOf(launch))
    }
}
