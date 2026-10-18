package ma.bosaj.updateradar

import android.content.Context
import android.os.Build
import android.content.pm.PackageManager

/** Version name from the package manager, so we don't need to enable BuildConfig generation. */
object BuildConfigCompat {
    fun versionName(context: Context): String = try {
        val pi = if (Build.VERSION.SDK_INT >= 33) context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        else @Suppress("DEPRECATION") context.packageManager.getPackageInfo(context.packageName, 0)
        pi.versionName ?: "?"
    } catch (_: Exception) {
        "?"
    }
}
