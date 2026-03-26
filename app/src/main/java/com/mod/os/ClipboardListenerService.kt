package com.mod.os

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

/**
 * Lightweight service that holds a ClipboardManager listener.
 * When the clipboard changes, it delegates to AccessibilityDelegateService
 * which launches ClipboardFocusActivity to perform the actual read.
 *
 * Separated from the accessibility service so it can be restarted independently.
 */
@AndroidEntryPoint
class ClipboardListenerService : Service() {

    private val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        AccessibilityServiceHolder.instance?.onClipboardChangedBySystem()
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onDestroy() {
        clipboardManager.removePrimaryClipChangedListener(listener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
