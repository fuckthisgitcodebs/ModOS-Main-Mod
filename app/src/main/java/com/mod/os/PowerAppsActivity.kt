package com.mod.os

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.contract.HostBridge
import com.mod.os.recents.ui.PowerAppsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PowerAppsActivity : ComponentActivity() {

    @Inject lateinit var hostBridge: HostBridge
    @Inject lateinit var repository: ClipboardRepository

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
                PowerAppsScreen(
                    hostBridge = hostBridge,
                    repository = repository
                )
            }
        }
    }
}
