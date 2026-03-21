package com.mod.os.recents.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mod.os.recents.clipboard.ClipboardRepository
import com.mod.os.recents.contract.HostBridge
import com.mod.os.recents.data.ClipEntry
import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.components.ActionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecentsViewModel @Inject constructor(
    private val repository: ClipboardRepository,
    private val hostBridge: HostBridge
) : ViewModel() {

    val activeClips: StateFlow<List<ClipEntry>> = repository
        .observeActiveClips()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val latestClip: StateFlow<ClipEntry?> = activeClips
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val suggestedActions: StateFlow<List<ActionItem>> = latestClip
        .map { clip -> clip?.let { deriveActions(it.clipType) } ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onActionSelected(action: ActionItem) {
        hostBridge.triggerHostAction(action.id, null)
    }

    private fun deriveActions(clipType: ClipType): List<ActionItem> = when (clipType) {
        ClipType.TEXT_URL -> listOf(
            ActionItem("open_url", "Open URL"),
            ActionItem("copy", "Copy"),
            ActionItem("share", "Share")
        )
        ClipType.TEXT_EMAIL -> listOf(
            ActionItem("compose_email", "Compose Email"),
            ActionItem("copy", "Copy")
        )
        ClipType.TEXT_PHONE -> listOf(
            ActionItem("dial", "Dial"),
            ActionItem("copy", "Copy")
        )
        ClipType.TEXT_ADDRESS -> listOf(
            ActionItem("open_maps", "Open Maps"),
            ActionItem("copy", "Copy")
        )
        ClipType.TEXT_HEX_COLOR -> listOf(
            ActionItem("copy", "Copy")
        )
        ClipType.TEXT_TERMINAL_COMMAND -> listOf(
            ActionItem("open_termux", "Open Termux"),
            ActionItem("copy", "Copy")
        )
        ClipType.TEXT_AI_PROMPT,
        ClipType.TEXT_AI_IMAGE_GEN_PROMPT -> listOf(
            ActionItem("copy", "Copy"),
            ActionItem("share", "Share")
        )
        ClipType.TEXT_CODE,
        ClipType.TEXT_CODE_BLOCK -> listOf(
            ActionItem("copy", "Copy"),
            ActionItem("share", "Share")
        )
        else -> listOf(
            ActionItem("copy", "Copy"),
            ActionItem("share", "Share")
        )
    }
}
