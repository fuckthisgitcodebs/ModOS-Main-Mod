package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl

    private var lastForegroundPackage: String? = null
    private var lastForegroundLabel: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityServiceHolder.instance = this
        startService(Intent(this, HighlightModeService::class.java))
        // FIX: Was never started — this is what triggers ClipboardFocusActivity
        // on clipboard change events, completing the capture pipeline
        startService(Intent(this, ClipboardListenerService::class.java))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
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

    fun onClipboardChangedBySystem() {
        val pkg = lastForegroundPackage ?: return
        val intent = ClipboardFocusActivity.createIntent(this, pkg, lastForegroundLabel)
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        AccessibilityServiceHolder.instance = null
        stopService(Intent(this, HighlightModeService::class.java))
        stopService(Intent(this, ClipboardListenerService::class.java))
        super.onDestroy()
    }
}

object AccessibilityServiceHolder {
    var instance: AccessibilityDelegateService? = null
}
