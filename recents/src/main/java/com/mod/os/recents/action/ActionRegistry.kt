package com.mod.os.recents.action

import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.components.ActionItem

object ActionRegistry {

    private val providers = mutableListOf<ActionProvider>()

    fun registerProvider(provider: ActionProvider) {
        providers.add(provider)
    }

    fun getSuggestedActionsFor(clipType: ClipType, content: String): List<ActionItem> {
        return providers.flatMap { it.getActions(clipType, content) }
    }
}

interface ActionProvider {
    fun getActions(clipType: ClipType, content: String): List<ActionItem>
}

// Initial built-in provider
class BuiltInActionProvider : ActionProvider {

    override fun getActions(clipType: ClipType, content: String): List<ActionItem> {
        return when (clipType) {
            ClipType.TEXT_URL -> listOf(
                ActionItem("open_url", "Open in Browser"),
                ActionItem("copy_clean", "Copy Clean URL")
            )
            ClipType.TEXT_HEX_COLOR -> listOf(
                ActionItem("copy_hex", "Copy HEX"),
                ActionItem("copy_rgb", "Copy RGB")
            )
            ClipType.TEXT_AI_IMAGE_GEN_PROMPT -> listOf(
                ActionItem("send_to_gen", "Send to Image Gen Bridge")
            )
            ClipType.TEXT_CODE_BLOCK -> listOf(
                ActionItem("dock_code", "Dock as Mini-Mod")
            )
            ClipType.TEXT_TERMINAL_COMMAND -> listOf(
                ActionItem("execute_termux", "Execute in Termux Bridge")
            )
            else -> emptyList()
        }
    }
}

// Register built-in at app/module init
fun initializeActionRegistry() {
    ActionRegistry.registerProvider(BuiltInActionProvider())
}
