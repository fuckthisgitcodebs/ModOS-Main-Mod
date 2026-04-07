package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ModOS_A11y", "onServiceConnected")
        AccessibilityServiceHolder.instance = this
        startService(Intent(this, HighlightModeService::class.java))
        startService(Intent(this, ClipboardListenerService::class.java))
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
            ClipboardListenerServiceHolder.instance?.updateForegroundApp(pkg, label)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        Log.d("ModOS_A11y", "onDestroy")
        AccessibilityServiceHolder.instance = null
        stopService(Intent(this, HighlightModeService::class.java))
        stopService(Intent(this, ClipboardListenerService::class.java))
        super.onDestroy()
    }
}

object AccessibilityServiceHolder {
    var instance: AccessibilityDelegateService? = null
}
