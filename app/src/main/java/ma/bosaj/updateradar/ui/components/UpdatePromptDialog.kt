package ma.bosaj.updateradar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class UpdatePromptMode {
    /** User can dismiss and continue using the app while the update downloads. */
    FLEXIBLE,

    /** Blocking update prompt for critical security releases. */
    IMMEDIATE,
}

data class UpdatePromptState(
    val appLabel: String,
    val currentVersion: String,
    val availableVersion: String,
    val sourceName: String = "GitHub",
    val releaseNotes: String = "",
    val mode: UpdatePromptMode = UpdatePromptMode.FLEXIBLE,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val isDownloading: Boolean = false,
    val sha256Verified: Boolean = false,
) {
    val progressFraction: Float
        get() = if (totalBytes > 0L) (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100f).toInt().coerceIn(0, 100)

    val isReadyToInstall: Boolean
        get() = totalBytes > 0L && bytesDownloaded >= totalBytes
}

/**
 * Material 3 customizable in-app update dialog and progress indicator supporting
 * both flexible and immediate update flows (#9).
 */
@Composable
fun InAppUpdateDialog(
    state: UpdatePromptState,
    onConfirmUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (state.mode == UpdatePromptMode.FLEXIBLE && !state.isDownloading) {
                onDismiss()
            }
        },
        icon = { Icon(Icons.Rounded.SystemUpdate, contentDescription = null) },
        title = {
            Text(
                text = "${state.appLabel} ${state.availableVersion}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "${state.currentVersion} → ${state.availableVersion} (${state.sourceName})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (state.sha256Verified) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Rounded.VerifiedUser, contentDescription = null)
                            Text(
                                text = "SHA-256 Checksum Verified",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                if (state.releaseNotes.isNotBlank()) {
                    Text(
                        text = state.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.isDownloading || state.isReadyToInstall) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { state.progressFraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "${state.progressPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmUpdate, enabled = !state.isDownloading || state.isReadyToInstall) {
                Text(if (state.isReadyToInstall) "Install" else "Update")
            }
        },
        dismissButton = {
            if (state.mode == UpdatePromptMode.FLEXIBLE) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        },
    )
}
