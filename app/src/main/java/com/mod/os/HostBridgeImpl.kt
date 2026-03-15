package com.mod.os

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import com.mod.os.recents.contract.ClipboardObserverCallback
import com.mod.os.recents.contract.HostBridge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostBridgeImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HostBridge {

    companion object {
        private const val POWER_APPS_KEY       = "recents_power_apps_packages"
        private const val SENSITIVE_PKGS_KEY   = "sensitive_packages"
        private const val PREFS_NAME           = "mod_os_prefs"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val clipboardCallbacks = mutableListOf<ClipboardObserverCallback>()

    override fun getSharedPreferences(): SharedPreferences = prefs

    // FIX: Was unconditionally calling onGranted(). Now checks whether
    // AccessibilityDelegateService is actually enabled before granting.
    // If not enabled, onGranted is NOT called — callers must handle that branch
    // (e.g. redirect to AccessibilityPermissionActivity).
    override fun requestAccessibilityPermissionIfNeeded(onGranted: () -> Unit) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isEnabled = am
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
        if (isEnabled) onGranted()
    }

    override fun triggerHostAction(actionId: String, payload: Bundle?) {
        // Future: dispatch to mod registry
    }

    override fun getPowerAppsPackageNames(): Set<String> =
        prefs.getStringSet(POWER_APPS_KEY, emptySet()) ?: emptySet()

    override fun setPowerAppsPackageNames(packages: Set<String>) {
        prefs.edit().putStringSet(POWER_APPS_KEY, packages).apply()
    }

    override fun registerClipboardObserver(callback: ClipboardObserverCallback) {
        synchronized(clipboardCallbacks) { clipboardCallbacks.add(callback) }
    }

    override fun unregisterClipboardObserver(callback: ClipboardObserverCallback) {
        synchronized(clipboardCallbacks) { clipboardCallbacks.remove(callback) }
    }

    // FIX: Was always returning null (full deferral to regex heuristics).
    // Now checks a prefs-backed set of user-flagged sensitive packages.
    // true  → explicitly sensitive (skip regex, mark immediately)
    // null  → unknown, defer to SensitivityDetector regex chain
    // Note: we don't store explicit "not sensitive" overrides yet — null covers
    // that case until a UI for per-package overrides is built.
    override fun isPackageSensitive(packageName: String): Boolean? {
        val sensitivePackages = prefs.getStringSet(SENSITIVE_PKGS_KEY, emptySet()) ?: emptySet()
        return if (sensitivePackages.contains(packageName)) true else null
    }

    override fun setPackageSensitive(packageName: String, sensitive: Boolean) {
        val current = prefs.getStringSet(SENSITIVE_PKGS_KEY, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
        if (sensitive) current.add(packageName) else current.remove(packageName)
        prefs.edit().putStringSet(SENSITIVE_PKGS_KEY, current).apply()
    }

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
