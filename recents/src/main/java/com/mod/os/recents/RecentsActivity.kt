package com.mod.os.recents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.mod.os.recents.ui.RecentsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentsActivity : ComponentActivity() {
    // FIX: Removed ClipboardMonitor injection — monitoring now lives in
    // AccessibilityDelegateService, independent of this activity's lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            androidx.compose.material3.MaterialTheme {
                RecentsScreen(
                    onDismiss = { finishAfterTransition() },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        }

        overridePendingTransition(
            R.anim.edge_slide_in,
            android.R.anim.fade_out
        )
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            android.R.anim.fade_in,
            R.anim.edge_slide_out
        )
    }
}
