package ma.bosaj.updateradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ma.bosaj.updateradar.BuildConfigCompat
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.RadarSettings
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.ui.RadarViewModel
import ma.bosaj.updateradar.ui.Screen
import ma.bosaj.updateradar.util.StoreLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: RadarViewModel, s: RadarSettings, galaxyInstalled: Boolean) {
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
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
            Group(stringResource(R.string.stores)) {
                Text(stringResource(R.string.stores_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { StoreLinks.openStoreHome(context, Store.PLAY) }) { Text(stringResource(R.string.store_play)) }
                    if (galaxyInstalled) OutlinedButton(onClick = { StoreLinks.openStoreHome(context, Store.GALAXY) }) { Text(stringResource(R.string.store_galaxy)) }
                }
            }
            Group(stringResource(R.string.privacy)) {
                Text(stringResource(R.string.privacy_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.version_line, BuildConfigCompat.versionName(context)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

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
