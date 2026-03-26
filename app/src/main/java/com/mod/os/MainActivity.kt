package com.mod.os

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mod.os.recents.RecentsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00D4FF),
                    secondary = Color(0xFF0288D1),
                    background = Color.Black,
                    surface = Color(0xFF121212)
                )
            ) {
                val overlayGranted = remember {
                    mutableStateOf(Settings.canDrawOverlays(this))
                }

                LaunchedEffect(Unit) {
                    overlayGranted.value = Settings.canDrawOverlays(this@MainActivity)
                }

                LauncherScreen(
                    overlayPermissionGranted = overlayGranted.value,
                    onLaunchRecents = {
                        startActivity(Intent(this, RecentsActivity::class.java))
                    },
                    onOpenPowerApps = {
                        startActivity(Intent(this, PowerAppsActivity::class.java))
                    },
                    onCheckAccessibility = {
                        startActivity(Intent(this, AccessibilityPermissionActivity::class.java))
                    },
                    onGrantOverlay = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                        )
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check overlay permission when returning from Settings
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00D4FF),
                    secondary = Color(0xFF0288D1),
                    background = Color.Black,
                    surface = Color(0xFF121212)
                )
            ) {
                val overlayGranted = Settings.canDrawOverlays(this)
                LauncherScreen(
                    overlayPermissionGranted = overlayGranted,
                    onLaunchRecents = {
                        startActivity(Intent(this, RecentsActivity::class.java))
                    },
                    onOpenPowerApps = {
                        startActivity(Intent(this, PowerAppsActivity::class.java))
                    },
                    onCheckAccessibility = {
                        startActivity(Intent(this, AccessibilityPermissionActivity::class.java))
                    },
                    onGrantOverlay = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun LauncherScreen(
    overlayPermissionGranted: Boolean,
    onLaunchRecents: () -> Unit,
    onOpenPowerApps: () -> Unit,
    onCheckAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ModOS Center Console",
            fontSize = 32.sp,
            color = Color(0xFF00D4FF),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Core hub for docked modules and intelligent clipboard synergy",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onLaunchRecents,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00D4FF),
                contentColor = Color.Black
            )
        ) { Text("Launch Recents Overlay", fontSize = 18.sp) }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenPowerApps,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White
            )
        ) { Text("Configure Power Apps", fontSize = 18.sp) }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCheckAccessibility,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Check Accessibility Permission", fontSize = 18.sp) }

        Spacer(modifier = Modifier.height(16.dp))

        // Overlay permission button — only shown if not yet granted
        if (!overlayPermissionGranted) {
            Button(
                onClick = onGrantOverlay,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B),
                    contentColor = Color.White
                )
            ) { Text("Grant Overlay Permission (Required)", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Required for highlight mode floating button",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "✓ Overlay permission granted",
                fontSize = 13.sp,
                color = Color(0xFF00D4FF).copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Available docking slots: 0/4 active\n(Modules will appear here)",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
