package ma.bosaj.updateradar.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.data.AppChange
import ma.bosaj.updateradar.data.InstalledApp
import ma.bosaj.updateradar.data.Radar
import ma.bosaj.updateradar.data.RadarSettings
import ma.bosaj.updateradar.data.Store
import ma.bosaj.updateradar.ui.RadarViewModel
import ma.bosaj.updateradar.ui.Screen
import ma.bosaj.updateradar.ui.Tab as RadarTab
import ma.bosaj.updateradar.ui.UiState
import ma.bosaj.updateradar.ui.components.AppIcon
import ma.bosaj.updateradar.ui.components.FreshDot
import ma.bosaj.updateradar.ui.components.RadarScope
import ma.bosaj.updateradar.ui.components.Stat
import ma.bosaj.updateradar.ui.components.StoreChip
import ma.bosaj.updateradar.ui.components.daysAgo
import ma.bosaj.updateradar.ui.components.freshColor
import ma.bosaj.updateradar.ui.components.storeName
import ma.bosaj.updateradar.ui.theme.LocalRadar
import ma.bosaj.updateradar.util.StoreLinks
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(vm: RadarViewModel, ui: UiState, s: RadarSettings) {
    val now = remember(ui.apps) { System.currentTimeMillis() }
    val summary = remember(ui.apps, s) { Radar.summary(ui.apps, now, s.agingAfterDays, s.staleAfterDays, s.ignored) }
    val list = remember(ui.apps, ui.filter, ui.sort, s) {
        Radar.apply(ui.apps, ui.filter, ui.sort, now, s.agingAfterDays, s.staleAfterDays, s.ignored)
    }
    val radar = LocalRadar.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Black)
                        if (s.lastScanAt > 0) {
                            Text(
                                stringResource(R.string.last_scan, DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(s.lastScanAt))),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) { Icon(Icons.Rounded.Refresh, stringResource(R.string.refresh)) }
                    IconButton(onClick = { vm.go(Screen.SETTINGS) }) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            if (summary.stale > 0 && ui.tab == RadarTab.APPS) {
                ExtendedFloatingActionButton(
                    onClick = { vm.startReview() },
                    icon = { Icon(Icons.Rounded.Update, null) },
                    text = { Text(pluralStringResource(R.plurals.review_stale, summary.stale, summary.stale)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item {
                if (ui.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                Box(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    RadarScope(ui.apps.filter { it.packageName !in s.ignored }, now, s.agingAfterDays, s.staleAfterDays, Modifier.widthIn(max = 320.dp).fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                summary.stale.toString(),
                                style = MaterialTheme.typography.displaySmall,
                                color = if (summary.stale > 0) radar.stale else radar.fresh,
                            )
                            Text(
                                pluralStringResource(R.plurals.stale_caption, summary.stale, s.staleAfterDays),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(140.dp),
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat(summary.fresh, stringResource(R.string.fresh), radar.fresh, Modifier.clickable { vm.onlyStale(false); vm.sort(Radar.Sort.RECENT) })
                    Stat(summary.aging, stringResource(R.string.aging), radar.aging)
                    Stat(summary.stale, stringResource(R.string.stale), radar.stale, Modifier.clickable { vm.onlyStale(!ui.filter.onlyStale) })
                    Stat(summary.total, stringResource(R.string.total), MaterialTheme.colorScheme.onSurface)
                }
                HonestyCard()
                TabRow(selectedTabIndex = ui.tab.ordinal, containerColor = MaterialTheme.colorScheme.background) {
                    Tab(ui.tab == RadarTab.APPS, onClick = { vm.tab(RadarTab.APPS) }, text = { Text(stringResource(R.string.tab_apps)) })
                    Tab(ui.tab == RadarTab.UPDATES, onClick = { vm.tab(RadarTab.UPDATES) }, text = {
                        Text(if (ui.feed.isEmpty()) stringResource(R.string.tab_updates) else stringResource(R.string.tab_updates_count, ui.feed.size))
                    })
                }
            }

            if (ui.tab == RadarTab.APPS) {
                item { Filters(vm, ui, summary.byStore) }
                if (list.isEmpty() && !ui.loading) item {
                    if (ui.apps.isEmpty() && !s.includeSystem) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.empty_none), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { vm.setIncludeSystem(true) }) { Text(stringResource(R.string.include_system)) }
                        }
                    } else {
                        Empty(stringResource(R.string.empty_filter))
                    }
                }
                items(list, key = { it.packageName }) { app ->
                    AppRow(app, now, s, vm, Modifier.animateItem())
                }
            } else {
                if (ui.feed.isEmpty()) item { Empty(stringResource(R.string.empty_feed)) }
                else item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { vm.clearFeed() }) { Text(stringResource(R.string.clear_feed)) }
                    }
                }
                items(ui.feed, key = { it.packageName + it.toVersion }) { c -> ChangeRow(c, vm, ui) }
            }
        }
    }
}

@Composable
private fun HonestyCard() {
    Card(
        Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.honesty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Filters(vm: RadarViewModel, ui: UiState, byStore: Map<Store, Int>) {
    var sortOpen by remember { mutableStateOf(false) }
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ui.filter.query,
                onValueChange = vm::query,
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { sortOpen = true }) { Icon(Icons.AutoMirrored.Rounded.Sort, stringResource(R.string.sort)) }
                DropdownMenu(sortOpen, onDismissRequest = { sortOpen = false }) {
                    listOf(Radar.Sort.STALEST to R.string.sort_stalest, Radar.Sort.RECENT to R.string.sort_recent, Radar.Sort.NAME to R.string.sort_name).forEach { (sort, label) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(label), fontWeight = if (ui.sort == sort) FontWeight.Bold else null) },
                            onClick = { vm.sort(sort); sortOpen = false },
                        )
                    }
                }
            }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(ui.filter.store == null && !ui.filter.onlyStale, onClick = { vm.store(null); vm.onlyStale(false) }, label = { Text(stringResource(R.string.all)) })
            }
            item {
                FilterChip(ui.filter.onlyStale, onClick = { vm.onlyStale(!ui.filter.onlyStale) }, label = { Text(stringResource(R.string.only_stale)) })
            }
            items(byStore.entries.sortedByDescending { it.value }.map { it.key }) { st ->
                FilterChip(
                    ui.filter.store == st,
                    onClick = { vm.store(if (ui.filter.store == st) null else st) },
                    label = { Text("${storeName(st)} · ${byStore[st]}") },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(app: InstalledApp, now: Long, s: RadarSettings, vm: RadarViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val f = Radar.freshness(app, now, s.agingAfterDays, s.staleAfterDays)
    val days = Radar.daysSince(app.lastUpdate, now)
    Row(
        modifier
            .fillMaxWidth()
            .clickable { vm.select(app) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(app.packageName, loader = vm.repo.scanner::icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FreshDot(f)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.updated_ago, daysAgo(days)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = freshColor(f),
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StoreChip(app.store)
                Spacer(Modifier.width(8.dp))
                Text("v${app.versionName}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = { StoreLinks.openStorePage(context, app.packageName, app.store, vm.ui.value.galaxyInstalled) }) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(R.string.open_store_for, app.label), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ChangeRow(c: AppChange, vm: RadarViewModel, ui: UiState) {
    val app = ui.apps.firstOrNull { it.packageName == c.packageName }
    Row(
        Modifier.fillMaxWidth().clickable(enabled = app != null) { app?.let(vm::select) }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(c.packageName, loader = vm.repo.scanner::icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(c.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${c.fromVersion}  →  ${c.toVersion}", style = MaterialTheme.typography.bodyMedium, color = LocalRadar.current.fresh, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(c.at)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Empty(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
