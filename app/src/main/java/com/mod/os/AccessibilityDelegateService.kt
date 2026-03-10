package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.ClipboardManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED ||
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Optional: detect app switches or copy actions
        }

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        if (clip != null) {
            val sourcePackage = event?.packageName?.toString()
            val sourceLabel = try {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(sourcePackage ?: "", 0)
                ).toString()
            } catch (e: Exception) {
                sourcePackage
            }

            hostBridge.notifyClipboardChanged(
                content = clip,
                mimeType = "text/plain",
                sourcePackage = sourcePackage,
                sourceLabel = sourceLabel
            )
        }
    }

    override fun onInterrupt() {}
}
