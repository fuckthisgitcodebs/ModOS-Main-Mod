package com.mod.os

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
                LauncherScreen(
                    onLaunchRecents = {
                        startActivity(Intent(this, RecentsActivity::class.java))
                    },
                    onOpenPowerApps = {
                        startActivity(Intent(this, PowerAppsActivity::class.java))
                    },
                    onCheckAccessibility = {
                        startActivity(Intent(this, AccessibilityPermissionActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun LauncherScreen(
    onLaunchRecents: () -> Unit,
    onOpenPowerApps: () -> Unit,
    onCheckAccessibility: () -> Unit,
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00D4FF),
                contentColor = Color.Black
            )
        ) {
            Text("Launch Recents Overlay", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenPowerApps,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White
            )
        ) {
            Text("Configure Power Apps", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCheckAccessibility,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Check Accessibility Permission", fontSize = 18.sp)
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
