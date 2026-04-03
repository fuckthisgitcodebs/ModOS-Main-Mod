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

    private var lastForegroundPackage: String? = null
    private var lastForegroundLabel: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ModOS_A11y", "onServiceConnected — setting holder, starting services")
        AccessibilityServiceHolder.instance = this
        startService(Intent(this, HighlightModeService::class.java))
        startService(Intent(this, ClipboardListenerService::class.java))
        Log.d("ModOS_A11y", "Services started. Holder = ${AccessibilityServiceHolder.instance}")
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
        val pkg = lastForegroundPackage
        Log.d("ModOS_Clip", "onClipboardChangedBySystem — foreground pkg = $pkg")
        if (pkg == null) {
            Log.w("ModOS_Clip", "foreground package is null — no copy event dispatched")
            return
        }
        val intent = ClipboardFocusActivity.createIntent(this, pkg, lastForegroundLabel)
        Log.d("ModOS_Clip", "Launching ClipboardFocusActivity for pkg=$pkg")
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        Log.d("ModOS_A11y", "onDestroy — nulling holder")
        AccessibilityServiceHolder.instance = null
        stopService(Intent(this, HighlightModeService::class.java))
        stopService(Intent(this, ClipboardListenerService::class.java))
        super.onDestroy()
    }
}

object AccessibilityServiceHolder {
    var instance: AccessibilityDelegateService? = null
}
