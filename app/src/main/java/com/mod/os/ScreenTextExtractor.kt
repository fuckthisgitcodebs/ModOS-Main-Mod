package com.mod.os

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo

object ScreenTextExtractor {

    data class ExtractedNode(
        val text: String,
        val bounds: Rect,
        val nodeClass: String?,
        val isClickable: Boolean
    )

    /**
     * Extract all text from all windows EXCEPT ModOS's own overlay windows.
     * This is critical — if we use rootInActiveWindow when our overlay is
     * visible, we get our own empty UI tree back, not the underlying app.
     */
    fun extractAllFromBackground(
        windows: List<AccessibilityWindowInfo>,
        ownPackage: String
    ): List<ExtractedNode> {
        val results = mutableListOf<ExtractedNode>()
        for (window in windows) {
            // Skip our own overlay windows
            val root = window.root ?: continue
            try {
                if (root.packageName?.toString() == ownPackage) {
                    root.recycle()
                    continue
                }
                traverseNode(root, results)
            } catch (_: Exception) {}
        }
        return results
    }

    /**
     * Find text at screen coordinates across all non-ModOS windows.
     */
    fun extractAtPointFromBackground(
        windows: List<AccessibilityWindowInfo>,
        ownPackage: String,
        x: Int,
        y: Int
    ): List<ExtractedNode> {
        val results = mutableListOf<ExtractedNode>()
        for (window in windows) {
            val root = window.root ?: continue
            try {
                if (root.packageName?.toString() == ownPackage) {
                    root.recycle()
                    continue
                }
                findNodeAtPoint(root, x, y, results)
            } catch (_: Exception) {}
        }
        // If nothing at exact point, fall back to full dump of background
        if (results.isEmpty()) {
            for (window in windows) {
                val root = window.root ?: continue
                try {
                    if (root.packageName?.toString() == ownPackage) {
                        root.recycle()
                        continue
                    }
                    traverseNode(root, results)
                } catch (_: Exception) {}
            }
        }
        return results
    }

    fun extractAll(root: AccessibilityNodeInfo?): List<ExtractedNode> {
        val results = mutableListOf<ExtractedNode>()
        traverseNode(root, results)
        return results
    }

    fun extractAtPoint(root: AccessibilityNodeInfo?, x: Int, y: Int): List<ExtractedNode> {
        val results = mutableListOf<ExtractedNode>()
        findNodeAtPoint(root, x, y, results)
        return results
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo?,
        results: MutableList<ExtractedNode>
    ) {
        node ?: return
        try {
            val text = node.text?.toString()?.takeIf { it.isNotBlank() }
                ?: node.contentDescription?.toString()?.takeIf { it.isNotBlank() }
                ?: node.hintText?.toString()?.takeIf { it.isNotBlank() }

            if (text != null) {
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                results.add(
                    ExtractedNode(
                        text = text,
                        bounds = bounds,
                        nodeClass = node.className?.toString(),
                        isClickable = node.isClickable
                    )
                )
            }
            for (i in 0 until node.childCount) {
                traverseNode(node.getChild(i), results)
            }
        } finally {
            try { node.recycle() } catch (_: Exception) {}
        }
    }

    private fun findNodeAtPoint(
        node: AccessibilityNodeInfo?,
        x: Int, y: Int,
        results: MutableList<ExtractedNode>
    ) {
        node ?: return
        try {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (bounds.contains(x, y)) {
                val text = node.text?.toString()?.takeIf { it.isNotBlank() }
                    ?: node.contentDescription?.toString()?.takeIf { it.isNotBlank() }
                if (text != null) {
                    results.add(
                        ExtractedNode(
                            text = text,
                            bounds = bounds,
                            nodeClass = node.className?.toString(),
                            isClickable = node.isClickable
                        )
                    )
                }
                for (i in 0 until node.childCount) {
                    findNodeAtPoint(node.getChild(i), x, y, results)
                }
            }
        } finally {
            try { node.recycle() } catch (_: Exception) {}
        }
    }
}
