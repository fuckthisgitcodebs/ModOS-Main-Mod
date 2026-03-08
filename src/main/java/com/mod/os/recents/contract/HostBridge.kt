package com.mod.os.recents.contract

import android.content.SharedPreferences
import android.os.Bundle
import com.mod.os.recents.clipboard.ClipboardObserverCallback

interface HostBridge {
    fun getSharedPreferences(): SharedPreferences

    fun requestAccessibilityPermissionIfNeeded(onGranted: () -> Unit)

    fun triggerHostAction(actionId: String, payload: Bundle?)

    fun getPowerAppsPackageNames(): Set<String>
    fun setPowerAppsPackageNames(packages: Set<String>)

    fun registerClipboardObserver(callback: ClipboardObserverCallback)
    fun unregisterClipboardObserver(callback: ClipboardObserverCallback)

    fun isPackageSensitive(packageName: String): Boolean?
}
