package ma.bosaj.updateradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.InstalledApp
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.RadarSettings
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.ui.RadarViewModel
import ma.bosaj.updateradar.ui.components.AppIcon
import ma.bosaj.updateradar.ui.components.FreshDot
import ma.bosaj.updateradar.ui.components.updatedLabel
import ma.bosaj.updateradar.ui.components.freshColor
import ma.bosaj.updateradar.ui.components.storeName
import ma.bosaj.updateradar.util.StoreLinks
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(app: InstalledApp, s: RadarSettings, galaxyInstalled: Boolean, vm: RadarViewModel) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val f = Radar.freshness(app, now, s.agingAfterDays, s.staleAfterDays)
    val target = StoreLinks.targetStore(app.store, galaxyInstalled)
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val factory = stringResource(R.string.factory_date)
    // Open fully: the half-height state hides the action buttons behind the navigation bar.
    ModalBottomSheet(onDismissRequest = { vm.select(null) }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app.packageName, size = 56.dp, loader = vm.repo.scanner::icon)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(app.label, style = MaterialTheme.typography.titleLarge)
                    Text(app.packageName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FreshDot(f)
                Spacer(Modifier.width(8.dp))
                Text(updatedLabel(app.lastUpdate, now), color = freshColor(f), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            Fact(stringResource(R.string.version), "${app.versionName} (${app.versionCode})")
            Fact(stringResource(R.string.installed_from), storeName(app.store) + (app.installerPackage?.let { "  ·  $it" } ?: ""))
            val dateOf = { t: Long -> if (Radar.isFactoryDate(t)) factory else date.format(Date(t)) }
            Fact(stringResource(R.string.last_updated), dateOf(app.lastUpdate))
            Fact(stringResource(R.string.first_installed), dateOf(app.firstInstall))
            if (app.store == Store.SIDELOADED || app.store == Store.OTHER_STORE) {
                Text(stringResource(R.string.sideload_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { StoreLinks.openStorePage(context, app.packageName, app.store, galaxyInstalled) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.open_in, storeName(target)))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { StoreLinks.openAppInfo(context, app.packageName) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Info, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.app_info))
                }
                OutlinedButton(onClick = { StoreLinks.openApp(context, app.packageName) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Apps, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.open_app))
                }
            }
            TextButton(onClick = { vm.ignore(app.packageName); vm.select(null) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.VisibilityOff, null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(if (app.packageName in s.ignored) R.string.unignore else R.string.ignore))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
