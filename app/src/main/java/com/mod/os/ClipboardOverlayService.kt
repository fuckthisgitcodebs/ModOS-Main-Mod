package com.mod.os

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import com.mod.os.recents.clipboard.ClipboardMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardOverlayService : Service() {

    @Inject lateinit var hostBridge: HostBridgeImpl
    @Inject lateinit var clipboardMonitor: ClipboardMonitor

    private var overlayView: View? = null
    private var lastClip: String? = null
    private var lastForegroundPackage: String? = null
    private var lastForegroundLabel: String? = null

    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(applicationContext)
            ?.toString()
            ?: return@OnPrimaryClipChangedListener
        if (clip.isBlank() || clip == lastClip) return@OnPrimaryClipChangedListener
        lastClip = clip
        val pkg = lastForegroundPackage ?: return@OnPrimaryClipChangedListener
        hostBridge.notifyClipboardChanged(
            content = clip,
            mimeType = "text/plain",
            sourcePackage = pkg,
            sourceLabel = lastForegroundLabel ?: pkg
        )
    }

    override fun onCreate() {
        super.onCreate()
        addOverlay()
        clipboardManager.addPrimaryClipChangedListener(clipListener)
        clipboardMonitor.startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "UPDATE_FOREGROUND") {
            lastForegroundPackage = intent.getStringExtra("pkg")
            lastForegroundLabel = intent.getStringExtra("label")
        }
        return START_STICKY
    }

    private fun addOverlay() {
        val params = WindowManager.LayoutParams(
            1, 1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSPARENT
        )
        val view = View(this).also { it.alpha = 0f }
        overlayView = view
        windowManager.addView(view, params)
    }

    override fun onDestroy() {
        clipboardMonitor.stopMonitoring()
        clipboardManager.removePrimaryClipChangedListener(clipListener)
        overlayView?.let { runCatching { windowManager.removeView(it) } }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
