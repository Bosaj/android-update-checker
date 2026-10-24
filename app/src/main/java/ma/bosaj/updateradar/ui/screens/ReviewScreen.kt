package ma.bosaj.updateradar.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ma.bosaj.updateradar.R
import ma.bosaj.updateradar.ui.RadarViewModel
import ma.bosaj.updateradar.ui.Screen
import ma.bosaj.updateradar.ui.UiState
import ma.bosaj.updateradar.ui.components.AppIcon
import ma.bosaj.updateradar.ui.components.StoreChip
import ma.bosaj.updateradar.ui.components.updatedLabel
import ma.bosaj.updateradar.ui.theme.LocalRadar
import ma.bosaj.updateradar.util.StoreLinks

/** Walk through the stale apps one by one: open the store page, update there, come back, next. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(vm: RadarViewModel, ui: UiState) {
    val context = LocalContext.current
    val total = ui.reviewQueue.size
    val current = ui.reviewQueue.getOrNull(ui.reviewIndex)?.let { pkg -> ui.apps.firstOrNull { it.packageName == pkg } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = {
                    IconButton(onClick = { vm.go(Screen.HOME); vm.refresh() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(20.dp)) {
            LinearProgressIndicator(
                progress = { if (total == 0) 1f else ui.reviewIndex.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.review_progress, ui.reviewIndex.coerceAtMost(total), total),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(24.dp))
            AnimatedContent(
                targetState = current,
                transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith fadeOut() },
                label = "review",
            ) { app ->
                if (app == null) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = LocalRadar.current.fresh, modifier = Modifier.height(72.dp).width(72.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(pluralStringResource(R.plurals.review_done, ui.reviewDone.size, ui.reviewDone.size), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { vm.go(Screen.HOME); vm.refresh() }) { Text(stringResource(R.string.back_to_radar)) }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            AppIcon(app.packageName, size = 84.dp, loader = vm.repo.scanner::icon)
                            Spacer(Modifier.height(16.dp))
                            Text(app.label, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                updatedLabel(app.lastUpdate, System.currentTimeMillis()),
                                color = LocalRadar.current.stale,
                            )
                            Spacer(Modifier.height(8.dp))
                            StoreChip(app.store)
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { StoreLinks.openStorePage(context, app.packageName, app.store, ui.galaxyInstalled) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.open_store_page))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { vm.reviewNext(false) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.skip)) }
                                Button(onClick = { vm.reviewNext(true) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.done_next)) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.review_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
