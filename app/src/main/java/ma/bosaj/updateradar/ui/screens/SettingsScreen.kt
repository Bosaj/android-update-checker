package ma.bosaj.updateradar.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlin.math.roundToInt
import ma.bosaj.updateradar.BuildConfigCompat
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.RadarSettings
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.data.UsageAccess
import ma.bosaj.updateradar.ui.RadarViewModel
import ma.bosaj.updateradar.ui.Screen
import ma.bosaj.updateradar.util.StoreLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: RadarViewModel, s: RadarSettings, galaxyInstalled: Boolean, usageAccess: Boolean) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { IconButton(onClick = { vm.go(Screen.HOME) }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        // Centred and capped on tablets and in landscape, full width on phones.
        Column(
            Modifier.fillMaxSize().padding(pad).wrapContentWidth().widthIn(max = 720.dp).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LanguageGroup()
            Group(stringResource(R.string.thresholds)) {
                var aging by remember(s.agingAfterDays) { mutableFloatStateOf(s.agingAfterDays.toFloat()) }
                var stale by remember(s.staleAfterDays) { mutableFloatStateOf(s.staleAfterDays.toFloat()) }
                Text(pluralStringResource(R.plurals.aging_after, aging.roundToInt(), aging.roundToInt()))
                Slider(aging, { aging = it; if (stale <= it) stale = it + 1 }, valueRange = 3f..60f, onValueChangeFinished = { vm.setThresholds(aging.roundToInt(), stale.roundToInt()) })
                Text(pluralStringResource(R.plurals.stale_after, stale.roundToInt(), stale.roundToInt()))
                Slider(stale, { stale = it.coerceAtLeast(aging + 1) }, valueRange = 7f..180f, onValueChangeFinished = { vm.setThresholds(aging.roundToInt(), stale.roundToInt()) })
            }
            Group(stringResource(R.string.reminders)) {
                Toggle(stringResource(R.string.reminders_on), stringResource(R.string.reminders_on_desc), s.remindersOn, vm::setReminders)
                Text(stringResource(R.string.every), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 7).forEach { d ->
                        FilterChip(s.reminderEveryDays == d, onClick = { vm.setEvery(d) }, enabled = s.remindersOn, label = { Text(pluralStringResource(R.plurals.every_days, d, d)) })
                    }
                }
                Toggle(stringResource(R.string.only_charging), stringResource(R.string.only_charging_desc), s.onlyWhileCharging, vm::setOnlyCharging)
            }
            Group(stringResource(R.string.apps_shown)) {
                Toggle(stringResource(R.string.include_system), stringResource(R.string.include_system_desc), s.includeSystem, vm::setIncludeSystem)
                if (s.ignored.isNotEmpty()) Text(pluralStringResource(R.plurals.ignored_count, s.ignored.size, s.ignored.size), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            UsageGroup(usageAccess)
            Group(stringResource(R.string.stores)) {
                Text(stringResource(R.string.stores_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { StoreLinks.openUpdatesPage(context, Store.PLAY) }) { Text(stringResource(R.string.store_play)) }
                    if (galaxyInstalled) OutlinedButton(onClick = { StoreLinks.openUpdatesPage(context, Store.GALAXY) }) { Text(stringResource(R.string.store_galaxy)) }
                }
            }
            Group(stringResource(R.string.privacy)) {
                Text(stringResource(R.string.privacy_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AboutGroup()
            Spacer(Modifier.height(24.dp))
        }
    }
}

private const val REPO = "https://github.com/Bosaj/android-update-checker"
private const val PRIVACY_POLICY = "$REPO/blob/main/docs/PRIVACY.md"

/** Every language the app is translated into, each written in its own language. */
private val LANGUAGES = listOf(
    "en" to "English",
    "fr" to "Français",
    "ar" to "العربية",
    "es" to "Español",
    "de" to "Deutsch",
    "it" to "Italiano",
    "pt-BR" to "Português (Brasil)",
    "tr" to "Türkçe",
)

/**
 * Per-app language. AppCompat stores the choice (in the system on Android 13+, in the app below) and
 * recreates the screen, so picking Arabic turns the whole layout right-to-left at once.
 */
@Composable
private fun LanguageGroup() {
    var open by remember { mutableStateOf(false) }
    val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val selected = LANGUAGES.firstOrNull { (tag, _) -> current.equals(tag, ignoreCase = true) || current.startsWith("$tag-", ignoreCase = true) }
    Group(stringResource(R.string.language)) {
        Row(
            Modifier.fillMaxWidth().clickable { open = true }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Language, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(selected?.second ?: stringResource(R.string.language_system), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.change), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                    LanguageOption(stringResource(R.string.language_system), selected == null) {
                        open = false
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    }
                    LANGUAGES.forEach { (tag, name) ->
                        LanguageOption(name, selected?.first == tag) {
                            open = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.close)) } },
        )
    }
}

@Composable
private fun LanguageOption(name: String, selected: Boolean, onPick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = onPick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Optional "last used" dates. Usage history is personal data, so the user reads a clear disclosure
 * before Android's Usage access screen opens (Google Play's prominent-disclosure rule).
 */
@Composable
private fun UsageGroup(granted: Boolean) {
    val context = LocalContext.current
    var disclose by remember { mutableStateOf(false) }
    Group(stringResource(R.string.usage_title)) {
        Text(stringResource(if (granted) R.string.usage_on else R.string.usage_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (granted) {
            OutlinedButton(onClick = { StoreLinks.launchFirst(context, UsageAccess.settingsIntents(context)) }) { Text(stringResource(R.string.open_settings)) }
        } else {
            FilledTonalButton(onClick = { disclose = true }) { Text(stringResource(R.string.usage_allow)) }
        }
    }
    if (disclose) {
        AlertDialog(
            onDismissRequest = { disclose = false },
            icon = { Icon(Icons.Rounded.History, null) },
            title = { Text(stringResource(R.string.usage_disclosure_title)) },
            text = { Text(stringResource(R.string.usage_disclosure)) },
            confirmButton = {
                TextButton(onClick = { disclose = false; StoreLinks.launchFirst(context, UsageAccess.settingsIntents(context)) }) { Text(stringResource(R.string.continue_btn)) }
            },
            dismissButton = { TextButton(onClick = { disclose = false }) { Text(stringResource(R.string.not_now)) } },
        )
    }
}

/** Version, privacy policy, open-source licences and source code. */
@Composable
private fun AboutGroup() {
    val context = LocalContext.current
    var licences by remember { mutableStateOf(false) }
    Group(stringResource(R.string.about)) {
        Text(stringResource(R.string.version_line, BuildConfigCompat.versionName(context)), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            OutlinedButton(onClick = { StoreLinks.openUrl(context, PRIVACY_POLICY) }) { Text(stringResource(R.string.privacy_policy)) }
            OutlinedButton(onClick = { licences = true }) { Text(stringResource(R.string.licences)) }
        }
        TextButton(onClick = { StoreLinks.openUrl(context, REPO) }) { Text(stringResource(R.string.source_code)) }
    }
    if (licences) {
        AlertDialog(
            onDismissRequest = { licences = false },
            title = { Text(stringResource(R.string.licences)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.licences_intro))
                    LICENCES.forEach { (name, licence) ->
                        Column {
                            Text(name, style = MaterialTheme.typography.titleSmall)
                            Text(licence, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { licences = false }) { Text(stringResource(R.string.close)) } },
            dismissButton = {
                TextButton(onClick = { StoreLinks.openUrl(context, "https://www.apache.org/licenses/LICENSE-2.0") }) { Text("Apache 2.0") }
            },
        )
    }
}

/** Third-party code shipped in the app, with its licence. Keep in step with gradle/libs.versions.toml. */
private val LICENCES = listOf(
    "Update Radar" to "MIT License · © 2026 Oussama EL HADJI",
    "AndroidX Core, AppCompat, Activity, Lifecycle, SplashScreen" to "Apache License 2.0 · © The Android Open Source Project",
    "Jetpack Compose, Material 3, Material Icons" to "Apache License 2.0 · © The Android Open Source Project",
    "WorkManager, DataStore" to "Apache License 2.0 · © The Android Open Source Project",
    "Kotlin standard library, kotlinx.coroutines" to "Apache License 2.0 · © JetBrains s.r.o. and contributors",
)

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun Toggle(title: String, desc: String, on: Boolean, set: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(on, set)
    }
}
