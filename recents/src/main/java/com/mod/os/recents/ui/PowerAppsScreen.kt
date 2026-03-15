package com.mod.os.recents.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.contract.HostBridge

@Composable
fun PowerAppsScreen(
    hostBridge: HostBridge,
    repository: ClipboardRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var currentPowerApps by remember { mutableStateOf(repository.getPowerApps()) }

    // FIX: Was a hardcoded list of 7 packages with no relation to what's actually
    // installed. Now queries PackageManager for user-installed apps only
    // (FLAG_SYSTEM excluded), sorted alphabetically by display label.
    val installedApps: List<Pair<String, String>> = remember {
        val pm = context.packageManager
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }
            .map { appInfo ->
                val label = pm.getApplicationLabel(appInfo).toString()
                appInfo.packageName to label
            }
            .sortedBy { (_, label) -> label.lowercase() }
    }

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
                            // Single write path through bridge (dual-prefs fix)
                            hostBridge.setPowerAppsPackageNames(updated)
                        }
                    )
                }
            }
        }
    }
}
