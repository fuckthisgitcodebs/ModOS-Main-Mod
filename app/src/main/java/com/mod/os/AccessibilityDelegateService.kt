package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Start the overlay service which handles clipboard capture
        startService(Intent(this, ClipboardOverlayService::class.java))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg == packageName || pkg == "com.android.systemui") return
            val label = try {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(pkg, 0)
                ).toString()
            } catch (e: Exception) {
                pkg.split(".").last()
            }
            // Forward foreground app context to overlay service
            val intent = Intent(this, ClipboardOverlayService::class.java).apply {
                action = "UPDATE_FOREGROUND"
                putExtra("pkg", pkg)
                putExtra("label", label)
            }
            startService(intent)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        stopService(Intent(this, ClipboardOverlayService::class.java))
        super.onDestroy()
    }
}
