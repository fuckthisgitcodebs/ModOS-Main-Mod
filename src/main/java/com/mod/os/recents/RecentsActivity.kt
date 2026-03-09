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
import androidx.lifecycle.lifecycleScope
import com.mod.os.recents.clipboard.ClipboardMonitor
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.ui.RecentsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecentsActivity : ComponentActivity() {

    @Inject lateinit var clipboardMonitor: ClipboardMonitor
    @Inject lateinit var repository: ClipboardRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            androidx.compose.material3.MaterialTheme {
                RecentsScreen(
                    repository = repository,
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

        lifecycleScope.launch {
            clipboardMonitor.startMonitoring()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            android.R.anim.fade_in,
            R.anim.edge_slide_out
        )
    }

    override fun onDestroy() {
        clipboardMonitor.stopMonitoring()
        super.onDestroy()
    }
}
