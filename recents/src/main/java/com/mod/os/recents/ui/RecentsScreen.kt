package com.mod.os.recents.ui

import android.content.Intent
import android.net.Uri
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mod.os.recents.data.ClipEntry
import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.components.ActionItem
import com.mod.os.recents.ui.components.LiaisonDeck
import com.mod.os.recents.ui.components.RecentsCardStack

@Composable
fun RecentsScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecentsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activeClips by viewModel.activeClips.collectAsStateWithLifecycle()
    val latestClip by viewModel.latestClip.collectAsStateWithLifecycle()
    val suggestedActions by viewModel.suggestedActions.collectAsStateWithLifecycle()

    var detailClip by remember { mutableStateOf<ClipEntry?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .systemBarsPadding()
    ) {
        RecentsCardStack(
            recents = activeClips.map { clip ->
                RecentApp(
                    packageName = clip.sourcePackage,
                    label = clip.appLabel ?: clip.sourcePackage.split(".").last(),
                    contentPreview = clip.contentPreview,
                    isSensitive = clip.isSensitive,
                    clipType = clip.clipType,
                    clipId = clip.id
                )
            },
            onCardClick = { app ->
                // Short tap — launch source app
                context.packageManager
                    .getLaunchIntentForPackage(app.packageName)
                    ?.let { context.startActivity(it) }
            },
            onCardLongClick = { app ->
                // Long tap — find matching ClipEntry and show detail sheet
                detailClip = activeClips.find { it.id == app.clipId }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height((LocalConfiguration.current.screenHeightDp * 0.60).dp)
        )

        LiaisonDeck(
            clipboardPreview = latestClip?.let { clip ->
                ClipboardPreview(
                    text = clip.contentPreview,
                    type = clip.clipType,
                    sourceApp = clip.appLabel ?: clip.sourcePackage
                )
            },
            detectedType = latestClip?.clipType,
            suggestedActions = suggestedActions,
            onActionSelected = { action ->
                handleAction(context, action, latestClip)
                viewModel.onActionSelected(action)
            },
            accentPrimary = Color(0xFF00D4FF),
            accentSecondary = Color(0xFF0288D1),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height((LocalConfiguration.current.screenHeightDp * 0.40).dp)
        )
    }

    // Detail bottom sheet — rendered outside the Box so it overlays everything
    detailClip?.let { clip ->
        ClipDetailBottomSheet(
            clip = clip,
            onDismiss = { detailClip = null }
        )
    }
}

/**
 * Handles known action IDs locally (copy, share, open_url, dial, open_maps).
 * Unknown IDs are forwarded to HostBridge via ViewModel.onActionSelected().
 */
private fun handleAction(context: Context, action: ActionItem, clip: ClipEntry?) {
    val content = clip?.fullContent ?: return
    when (action.id) {
        "copy" -> {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("ModOS clip", content))
        }
        "share" -> {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
        "open_url" -> {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content.trim())))
            }
        }
        "dial" -> {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${content.trim()}")))
            }
        }
        "compose_email" -> {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${content.trim()}")))
            }
        }
        "open_maps" -> {
            runCatching {
                val uri = Uri.parse("geo:0,0?q=${Uri.encode(content.trim())}")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
        "open_termux" -> {
            runCatching {
                context.packageManager
                    .getLaunchIntentForPackage("com.termux")
                    ?.let { context.startActivity(it) }
            }
        }
        // All other IDs fall through to HostBridge via viewModel.onActionSelected()
    }
}

data class RecentApp(
    val packageName: String,
    val label: String,
    val contentPreview: String,
    val isSensitive: Boolean,
    val clipType: ClipType,
    val clipId: Long
)

data class ClipboardPreview(
    val text: String,
    val type: ClipType,
    val sourceApp: String
)
