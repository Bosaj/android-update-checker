package ma.bosaj.updateradar.data

/** Where an app was installed from, as reported by the system. */
enum class Store(val packageName: String?) {
    PLAY("com.android.vending"),
    GALAXY("com.sec.android.app.samsungapps"),
    AMAZON("com.amazon.venezia"),
    HUAWEI("com.huawei.appmarket"),
    FDROID("org.fdroid.fdroid"),
    OTHER_STORE(null),
    SIDELOADED(null),
    PREINSTALLED(null);

    companion object {
        private val known = mapOf(
            "com.android.vending" to PLAY,
            "com.google.android.feedback" to PLAY,
            "com.sec.android.app.samsungapps" to GALAXY,
            "com.amazon.venezia" to AMAZON,
            "com.huawei.appmarket" to HUAWEI,
            "org.fdroid.fdroid" to FDROID,
            "org.fdroid.basic" to FDROID,
        )
        /** Package installers (file managers, browsers, adb) that mean "installed by hand". */
        private val manual = setOf(
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.samsung.android.packageinstaller",
            "com.android.shell",
        )

        fun fromInstaller(installer: String?, isSystem: Boolean): Store = when {
            installer == null -> if (isSystem) PREINSTALLED else SIDELOADED
            installer in known -> known.getValue(installer)
            installer in manual -> SIDELOADED
            else -> OTHER_STORE
        }
    }
}

data class InstalledApp(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    /** Epoch millis of the last install or update. */
    val lastUpdate: Long,
    val firstInstall: Long,
    val store: Store,
    val installerPackage: String?,
    val isSystem: Boolean,
)

enum class Freshness { FRESH, AGING, STALE }

/** One app whose version changed between two scans. */
data class AppChange(
    val packageName: String,
    val label: String,
    val fromVersion: String,
    val toVersion: String,
    val at: Long,
)
