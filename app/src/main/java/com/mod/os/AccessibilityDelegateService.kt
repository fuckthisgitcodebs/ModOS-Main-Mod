package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.ClipData
import android.content.ClipboardManager
import javax.inject.Inject

class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED ||
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Optional: detect app switches or copy actions
        }

        // Primary clipboard monitoring (simplified)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        if (clip != null) {
            val sourcePackage = event?.packageName?.toString()
            val sourceLabel = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sourcePackage ?: "", 0)
            ).toString()

            hostBridge.notifyClipboardChanged(
                content = clip,
                mimeType = "text/plain", // can be improved with real mime
                sourcePackage = sourcePackage,
                sourceLabel = sourceLabel
            )
        }
    }

    override fun onInterrupt() {}
}
