package com.mod.os

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import com.mod.os.recents.clipboard.ClipboardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardFocusActivity : ComponentActivity() {

    @Inject lateinit var repository: ClipboardRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ModOS_Focus", "ClipboardFocusActivity onCreate")
        window.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            addFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            attributes = attributes.also { params ->
                params.width = 1
                params.height = 1
                params.alpha = 0f
                params.dimAmount = 0f
            }
        }
        setContentView(android.R.id.content)
    }

    override fun onResume() {
        super.onResume()
        Log.d("ModOS_Focus", "ClipboardFocusActivity onResume — attempting clipboard read")

        val sourcePackage = intent.getStringExtra(EXTRA_SOURCE_PACKAGE) ?: "unknown"
        val sourceLabel = intent.getStringExtra(EXTRA_SOURCE_LABEL) ?: "Clipboard"

        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(applicationContext)
            ?.toString()
            ?.takeIf { it.isNotBlank() }

        Log.d("ModOS_Focus", "Clipboard read result: ${if (clip != null) "GOT content (${clip.take(40)})" else "NULL — denied or empty"}")

        if (clip != null) {
            scope.launch {
                repository.addClipboardContent(
                    content = clip,
                    mimeType = "text/plain",
                    sourcePackage = sourcePackage,
                    sourceLabel = sourceLabel
                )
                Log.d("ModOS_Focus", "Stored clip from $sourcePackage")
            }
        }

        finishAndRemoveTask()
    }

    companion object {
        const val EXTRA_SOURCE_PACKAGE = "source_package"
        const val EXTRA_SOURCE_LABEL = "source_label"

        fun createIntent(context: Context, sourcePackage: String, sourceLabel: String?) =
            Intent(context, ClipboardFocusActivity::class.java).apply {
                putExtra(EXTRA_SOURCE_PACKAGE, sourcePackage)
                putExtra(EXTRA_SOURCE_LABEL, sourceLabel)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION
                            or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            or Intent.FLAG_ACTIVITY_NO_HISTORY
                )
            }
    }
}
