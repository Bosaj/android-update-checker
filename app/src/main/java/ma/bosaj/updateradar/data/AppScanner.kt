package ma.bosaj.updateradar.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

/** Reads the installed apps that have a launcher icon (the only ones this app may see without QUERY_ALL_PACKAGES). */
class AppScanner(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    fun scan(includeSystem: Boolean): List<InstalledApp> {
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val packages = queryLauncher(launcher).map { it.activityInfo.packageName }.toSet() - context.packageName
        return packages.mapNotNull { pkg -> read(pkg) }
            .filter { includeSystem || !it.isSystem || it.store != Store.PREINSTALLED }
    }

    private fun queryLauncher(intent: Intent) =
        if (Build.VERSION.SDK_INT >= 33) pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        else @Suppress("DEPRECATION") pm.queryIntentActivities(intent, 0)

    private fun info(pkg: String): PackageInfo? = try {
        if (Build.VERSION.SDK_INT >= 33) pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        else @Suppress("DEPRECATION") pm.getPackageInfo(pkg, 0)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    private fun installerOf(pkg: String): String? = try {
        if (Build.VERSION.SDK_INT >= 30) {
            val src = pm.getInstallSourceInfo(pkg)
            // The package that last updated/installed the app; fall back to the initiating package.
            src.installingPackageName ?: src.initiatingPackageName
        } else {
            @Suppress("DEPRECATION") pm.getInstallerPackageName(pkg)
        }
    } catch (_: Exception) {
        null
    }

    fun read(pkg: String): InstalledApp? {
        val pi = info(pkg) ?: return null
        val ai = pi.applicationInfo ?: return null
        val isSystem = ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
        val installer = installerOf(pkg)
        val code = if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode else @Suppress("DEPRECATION") pi.versionCode.toLong()
        return InstalledApp(
            packageName = pkg,
            label = pm.getApplicationLabel(ai).toString(),
            versionName = pi.versionName ?: code.toString(),
            versionCode = code,
            lastUpdate = pi.lastUpdateTime,
            firstInstall = pi.firstInstallTime,
            store = Store.fromInstaller(installer, isSystem),
            installerPackage = installer,
            isSystem = isSystem,
        )
    }

    fun icon(pkg: String): Drawable? = try {
        pm.getApplicationIcon(pkg)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    fun isInstalled(pkg: String): Boolean = info(pkg) != null
}
