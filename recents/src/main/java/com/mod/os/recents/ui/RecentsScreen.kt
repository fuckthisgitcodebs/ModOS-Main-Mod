package com.mod.os.recents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.components.LiaisonDeck
import com.mod.os.recents.ui.components.RecentsCardStack

@Composable
fun RecentsScreen(
    repository: ClipboardRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeClips by repository.observeActiveClips()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val latestClip = activeClips.firstOrNull()

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
                    previewUri = null,
                    isSensitive = clip.isSensitive,
                    clipType = clip.clipType
                )
            },
            onCardClick = { /* Delegate to host via bridge */ },
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
            suggestedActions = emptyList(), // Placeholder — to be replaced with registry
            onActionSelected = { /* TODO */ },
            accentPrimary = Color(0xFF00D4FF),
            accentSecondary = Color(0xFF0288D1),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height((LocalConfiguration.current.screenHeightDp * 0.40).dp)
        )
    }
}

data class RecentApp(
    val packageName: String,
    val label: String,
    val previewUri: String?,
    val isSensitive: Boolean,
    val clipType: ClipType
)

data class ClipboardPreview(
    val text: String,
    val type: ClipType,
    val sourceApp: String
)
