package com.mod.os

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.mod.os.recents.clipboard.ClipboardObserverCallback
import com.mod.os.recents.contract.HostBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostBridgeImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HostBridge {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("mod_os_prefs", Context.MODE_PRIVATE)
    }

    private val clipboardCallbacks = mutableListOf<ClipboardObserverCallback>()

    override fun getSharedPreferences(): SharedPreferences = prefs

    override fun requestAccessibilityPermissionIfNeeded(onGranted: () -> Unit) {
        // Host can show UI prompt here; for now just assume granted
        onGranted()
    }

    override fun triggerHostAction(actionId: String, payload: Bundle?) {
        // Future: handle actions from modules (e.g., create note, launch external)
    }

    override fun getPowerAppsPackageNames(): Set<String> {
        return prefs.getStringSet("power_apps_packages", emptySet()) ?: emptySet()
    }

    override fun setPowerAppsPackageNames(packages: Set<String>) {
        prefs.edit().putStringSet("power_apps_packages", packages).apply()
    }

    override fun registerClipboardObserver(callback: ClipboardObserverCallback) {
        synchronized(clipboardCallbacks) {
            clipboardCallbacks.add(callback)
        }
    }

    override fun unregisterClipboardObserver(callback: ClipboardObserverCallback) {
        synchronized(clipboardCallbacks) {
            clipboardCallbacks.remove(callback)
        }
    }

    override fun isPackageSensitive(packageName: String): Boolean? {
        // Host-level override; return null to fall back to pattern detection
        return null
    }

    // Called by AccessibilityService when clipboard changes
    fun notifyClipboardChanged(
        content: String?,
        mimeType: String?,
        sourcePackage: String?,
        sourceLabel: String?
    ) {
        synchronized(clipboardCallbacks) {
            clipboardCallbacks.forEach {
                it.onClipboardChanged(content, mimeType, sourcePackage, sourceLabel)
            }
        }
    }
}
