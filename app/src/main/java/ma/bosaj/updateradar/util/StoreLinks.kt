package ma.bosaj.updateradar.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import ma.bosaj.updateradar.data.Store

/**
 * Opens the right store page for an app. A normal app cannot start an update for another app,
 * so the best it can do is take the user to the page where the store's own Update button is.
 */
object StoreLinks {
    /** The store whose page we open: the installer when we know it, otherwise Play Store. */
    fun targetStore(store: Store, galaxyInstalled: Boolean): Store = when (store) {
        Store.GALAXY -> if (galaxyInstalled) Store.GALAXY else Store.PLAY
        else -> Store.PLAY
    }

    fun pageIntents(pkg: String, store: Store): List<Intent> = when (store) {
        Store.GALAXY -> listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/$pkg")).setPackage(Store.GALAXY.packageName),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxystore.samsung.com/detail/$pkg")),
        )
        else -> listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).setPackage(Store.PLAY.packageName),
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")),
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
        launchFirst(context, pageIntents(pkg, targetStore(store, galaxyInstalled)))

    /** Opens a store's own app so the user can tap its Updates tab. */
    fun openStoreHome(context: Context, store: Store): Boolean {
        val pkg = store.packageName ?: return false
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return false
        return launchFirst(context, listOf(launch))
    }

    fun openAppInfo(context: Context, pkg: String): Boolean = launchFirst(
        context,
        listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)),
    )

    fun openApp(context: Context, pkg: String): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return false
        return launchFirst(context, listOf(launch))
    }
}
