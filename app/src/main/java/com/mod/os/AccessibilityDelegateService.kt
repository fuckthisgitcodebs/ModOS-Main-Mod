package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl

    private var lastClip: String? = null
    private var lastForegroundPackage: String? = null
    private var lastForegroundLabel: String? = null

    private val clipboardManager by lazy {
        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
        if (!clip.isNullOrBlank() && clip != lastClip) {
            lastClip = clip
            val pkg = lastForegroundPackage ?: return@OnPrimaryClipChangedListener
            hostBridge.notifyClipboardChanged(
                content = clip,
                mimeType = "text/plain",
                sourcePackage = pkg,
                sourceLabel = lastForegroundLabel ?: pkg
            )
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboardManager.addPrimaryClipChangedListener(clipListener)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            // Ignore system UI and our own app
            if (pkg == packageName || pkg == "com.android.systemui") return
            lastForegroundPackage = pkg
            lastForegroundLabel = try {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(pkg, 0)
                ).toString()
            } catch (e: Exception) {
                pkg.split(".").last()
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager.removePrimaryClipChangedListener(clipListener)
    }
}
