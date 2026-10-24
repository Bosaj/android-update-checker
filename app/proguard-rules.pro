# WorkManager creates workers by reflection from their class name and (Context, WorkerParameters) constructor.
-keep class ma.bosaj.updateradar.work.ReminderWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

# Strip verbose and debug logging from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
