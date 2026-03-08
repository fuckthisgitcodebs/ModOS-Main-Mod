package com.mod.os.minmod.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mod.os.recents.data.ClipEntry
import com.mod.os.recents.data.ClipType
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class NotesMiniMod @Inject constructor() {

    // Called by host docking manager to determine if this mini-mod wants to display these clips
    fun shouldDock(clips: List<ClipEntry>): Boolean {
        return clips.any { clip ->
            clip.clipType in listOf(
                ClipType.TEXT_PLAIN,
                ClipType.TEXT_MARKDOWN,
                ClipType.TEXT_CODE_BLOCK
            )
        }
    }

    @Composable
    fun DockContent(
        relevantClips: List<ClipEntry>,
        onOpenNoteEditor: (ClipEntry) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier.padding(16.dp)) {
            Text(
                text = "Notes Mini-Mod",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF00D4FF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(relevantClips) { clip ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onOpenNoteEditor(clip) },
                        shape = MaterialTheme.shapes.medium,
                        color = Color(0xFF1E1E1E)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = clip.clipType.name.replace("TEXT_", ""),
                                color = Color(0xFF00D4FF),
                                modifier = Modifier.weight(0.3f)
                            )
                            Text(
                                text = clip.contentPreview.take(60) + if (clip.contentPreview.length > 60) "..." else "",
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
