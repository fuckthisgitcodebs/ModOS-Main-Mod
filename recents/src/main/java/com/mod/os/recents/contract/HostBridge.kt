package com.mod.os.recents.contract

import android.content.SharedPreferences
import android.os.Bundle

interface HostBridge {
    fun getSharedPreferences(): SharedPreferences

    fun requestAccessibilityPermissionIfNeeded(onGranted: () -> Unit)

    fun triggerHostAction(actionId: String, payload: Bundle?)

    fun getPowerAppsPackageNames(): Set<String>
    fun setPowerAppsPackageNames(packages: Set<String>)

    fun registerClipboardObserver(callback: ClipboardObserverCallback)
    fun unregisterClipboardObserver(callback: ClipboardObserverCallback)

    /**
     * Returns true if [packageName] is explicitly user-flagged sensitive,
     * false if explicitly flagged non-sensitive, null to defer to
     * SensitivityDetector's regex heuristics.
     */
    fun isPackageSensitive(packageName: String): Boolean?

    // FIX: Exposed so UI (future settings screen) or tests can mark/unmark
    // packages without reaching into HostBridgeImpl directly.
    fun setPackageSensitive(packageName: String, sensitive: Boolean)
}
