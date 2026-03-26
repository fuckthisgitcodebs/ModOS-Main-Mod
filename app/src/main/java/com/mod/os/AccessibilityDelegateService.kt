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
    private var highlightModeService: HighlightModeService? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Start highlight mode floating button
        startService(Intent(this, HighlightModeService::class.java))

        // Give HighlightModeService a reference back to us for rootInActiveWindow
        // We do this via a companion object singleton reference — clean enough
        // for a service-to-service local ref, no IPC needed
        AccessibilityServiceHolder.instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        when (event.eventType) {

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
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

            // Clipboard changed — launch transparent focus Activity to read it
            // This is the focus-stealer approach: Activity takes genuine window
            // focus, satisfying Samsung's ClipboardService foreground check
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                // Not the right hook for copy — handled via ClipboardManager listener
                // in ClipboardFocusActivity. Left here for future clipboard-adjacent use.
            }
        }
    }

    // Called by ClipboardManager listener if we wire one here in future
    fun onClipboardChangedBySystem() {
        val pkg = lastForegroundPackage ?: return
        val intent = ClipboardFocusActivity.createIntent(this, pkg, lastForegroundLabel)
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        AccessibilityServiceHolder.instance = null
        stopService(Intent(this, HighlightModeService::class.java))
        super.onDestroy()
    }
}

/**
 * Simple singleton holder so HighlightModeService can access
 * rootInActiveWindow without a full IPC binding.
 * Nulled on service destroy.
 */
object AccessibilityServiceHolder {
    var instance: AccessibilityDelegateService? = null
}
