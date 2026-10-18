package ma.bosaj.updateradar.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore(name = "settings")

data class RadarSettings(
    val agingAfterDays: Int = 14,
    val staleAfterDays: Int = 45,
    val remindersOn: Boolean = true,
    val reminderEveryDays: Int = 3,
    val onlyWhileCharging: Boolean = false,
    val includeSystem: Boolean = false,
    val ignored: Set<String> = emptySet(),
    val onboarded: Boolean = false,
    val lastScanAt: Long = 0L,
)

class SettingsRepo(private val context: Context) {
    private object K {
        val aging = intPreferencesKey("aging_days")
        val stale = intPreferencesKey("stale_days")
        val reminders = booleanPreferencesKey("reminders_on")
        val every = intPreferencesKey("reminder_every_days")
        val charging = booleanPreferencesKey("only_charging")
        val system = booleanPreferencesKey("include_system")
        val ignored = stringSetPreferencesKey("ignored")
        val onboarded = booleanPreferencesKey("onboarded")
        val lastScan = longPreferencesKey("last_scan_at")
    }

    val flow: Flow<RadarSettings> = context.store.data.map { p -> p.toSettings() }

    suspend fun current(): RadarSettings = flow.first()

    private fun Preferences.toSettings() = RadarSettings(
        agingAfterDays = this[K.aging] ?: 14,
        staleAfterDays = this[K.stale] ?: 45,
        remindersOn = this[K.reminders] ?: true,
        reminderEveryDays = this[K.every] ?: 3,
        onlyWhileCharging = this[K.charging] ?: false,
        includeSystem = this[K.system] ?: false,
        ignored = this[K.ignored] ?: emptySet(),
        onboarded = this[K.onboarded] ?: false,
        lastScanAt = this[K.lastScan] ?: 0L,
    )

    suspend fun setThresholds(aging: Int, stale: Int) = context.store.edit {
        it[K.aging] = aging.coerceIn(1, 365)
        it[K.stale] = stale.coerceIn(aging + 1, 730)
    }
    suspend fun setReminders(on: Boolean) = context.store.edit { it[K.reminders] = on }
    suspend fun setEvery(days: Int) = context.store.edit { it[K.every] = days.coerceIn(1, 30) }
    suspend fun setOnlyCharging(on: Boolean) = context.store.edit { it[K.charging] = on }
    suspend fun setIncludeSystem(on: Boolean) = context.store.edit { it[K.system] = on }
    suspend fun setOnboarded() = context.store.edit { it[K.onboarded] = true }
    suspend fun setLastScan(at: Long) = context.store.edit { it[K.lastScan] = at }
    suspend fun toggleIgnored(pkg: String) = context.store.edit {
        val cur = it[K.ignored] ?: emptySet()
        it[K.ignored] = if (pkg in cur) cur - pkg else cur + pkg
    }
}
