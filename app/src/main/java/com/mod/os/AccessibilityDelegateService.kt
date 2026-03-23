package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.view.accessibility.AccessibilityEvent
import com.mod.os.recents.clipboard.ClipboardMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl
    // FIX: ClipboardMonitor starts here, not in RecentsActivity.
    // Monitoring lifetime = service lifetime, not activity lifetime.
    @Inject lateinit var clipboardMonitor: ClipboardMonitor

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
        // FIX: Start monitoring here so clips are captured regardless of
        // whether RecentsActivity is open
        clipboardMonitor.startMonitoring()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
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
        clipboardMonitor.stopMonitoring()
        clipboardManager.removePrimaryClipChangedListener(clipListener)
        super.onDestroy()
    }
}
