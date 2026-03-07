package com.mod.os.recents.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.contract.HostBridge

@Composable
fun PowerAppsScreen(
    hostBridge: HostBridge,
    repository: ClipboardRepository,
    modifier: Modifier = Modifier
) {
    var currentPowerApps by remember { mutableStateOf(repository.getPowerApps()) }

    // Placeholder installed apps list — in production, query PackageManager via host
    val installedApps = listOf(
        "com.android.chrome" to "Chrome",
        "com.termux" to "Termux",
        "com.google.android.gm" to "Gmail",
        "com.discord" to "Discord",
        "org.telegram.messenger" to "Telegram",
        "com.google.android.apps.photos" to "Photos",
        "com.whatsapp" to "WhatsApp"
    )

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Power Apps Configuration",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selected apps receive 30 active clips + infinite archive.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(installedApps) { (pkg, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Checkbox(
                        checked = currentPowerApps.contains(pkg),
                        onCheckedChange = { checked ->
                            val updated = if (checked) {
                                currentPowerApps + pkg
                            } else {
                                currentPowerApps - pkg
                            }
                            currentPowerApps = updated
                            hostBridge.setPowerAppsPackageNames(updated)
                            repository.setPowerApps(updated)
                        }
                    )
                }
            }
        }
    }
}
