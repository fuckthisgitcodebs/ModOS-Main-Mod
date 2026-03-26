package com.mod.os

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Utility for extracting text from the accessibility node tree.
 * Handles both selectable and non-selectable text — buttons, labels,
 * notifications, status bar elements, anything the OS renders as a View.
 *
 * Two extraction modes:
 * - extractAll(): dumps every text string visible on screen
 * - extractAtPoint(): finds the node at/nearest to given screen coordinates
 *   and returns its text plus its ancestors' context text
 */
object ScreenTextExtractor {

    data class ExtractedNode(
        val text: String,
        val bounds: Rect,
        val nodeClass: String?,
        val isClickable: Boolean
    )

    /**
     * Recursively walk the full node tree from root, collecting all
     * text and content descriptions regardless of selectability.
     */
    fun extractAll(root: AccessibilityNodeInfo?): List<ExtractedNode> {
        val results = mutableListOf<ExtractedNode>()
        traverseNode(root, results)
        return results
    }

    /**
     * Find the most specific node whose bounds contain the given screen
     * coordinates, then return its text and its immediate context.
     */
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
            node.recycle()
        }
    }

    private fun findNodeAtPoint(
        node: AccessibilityNodeInfo?,
        x: Int,
        y: Int,
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

                // Recurse into children to find the most specific hit
                for (i in 0 until node.childCount) {
                    findNodeAtPoint(node.getChild(i), x, y, results)
                }
            }
        } finally {
            node.recycle()
        }
    }
}
