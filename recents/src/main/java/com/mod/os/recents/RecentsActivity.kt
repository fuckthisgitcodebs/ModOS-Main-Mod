package com.mod.os.recents

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.ui.RecentsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecentsActivity : ComponentActivity() {

    @Inject lateinit var repository: ClipboardRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        overridePendingTransition(R.anim.edge_slide_in, android.R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        // Read clipboard while we have foreground focus — bypasses Android 12+ restriction
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip?.getItemAt(0)
            ?.coerceToText(applicationContext)
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: return

        // Determine foreground package from the clip source if possible,
        // fall back to unknown
        scope.launch {
            repository.addClipboardContent(
                content = clip,
                mimeType = "text/plain",
                sourcePackage = "unknown",
                sourceLabel = "Clipboard"
            )
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.edge_slide_out)
    }
}
