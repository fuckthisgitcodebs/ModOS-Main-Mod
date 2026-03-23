package com.mod.os

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.view.accessibility.AccessibilityEvent
import com.mod.os.recents.clipboard.ClipboardMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityDelegateService : AccessibilityService() {

    @Inject lateinit var hostBridge: HostBridgeImpl
    @Inject lateinit var clipboardMonitor: ClipboardMonitor

    private var lastForegroundPackage: String? = null
    private var lastForegroundLabel: String? = null

    // Last text seen via selection events — used as clipboard content
    // since Android 12+ denies direct ClipboardManager reads from services
    private var lastSelectedText: String? = null
    private var lastDeliveredClip: String? = null

    private val clipboardManager by lazy {
        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    // Clipboard listener: used as a TRIGGER only, not for reading content.
    // Content comes from lastSelectedText captured via accessibility events.
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val text = lastSelectedText ?: return@OnPrimaryClipChangedListener
        val pkg = lastForegroundPackage ?: return@OnPrimaryClipChangedListener
        if (text == lastDeliveredClip) return@OnPrimaryClipChangedListener
        lastDeliveredClip = text
        hostBridge.notifyClipboardChanged(
            content = text,
            mimeType = "text/plain",
            sourcePackage = pkg,
            sourceLabel = lastForegroundLabel ?: pkg
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboardManager.addPrimaryClipChangedListener(clipListener)
        clipboardMonitor.startMonitoring()
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

            // KEY FIX: Intercept text at selection time — this fires when the
            // user selects/copies text and carries the content in event.text.
            // Android 12+ allows accessibility services to read this even when
            // not in focus, unlike direct ClipboardManager reads.
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                val texts = event.text
                if (!texts.isNullOrEmpty()) {
                    val combined = texts.joinToString("") { it }
                    if (combined.isNotBlank()) {
                        lastSelectedText = combined
                    }
                }
            }

            // Also capture from announcements — some apps (Chrome, Samsung
            // Browser) announce "Copied to clipboard" as an accessibility event
            AccessibilityEvent.TYPE_ANNOUNCEMENT -> {
                val source = event.source ?: return
                try {
                    val node = source
                    val nodeText = node.text?.toString()
                    if (!nodeText.isNullOrBlank()) {
                        lastSelectedText = nodeText
                    }
                    node.recycle()
                } catch (_: Exception) {}
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        clipboardMonitor.stopMonitoring()
        clipboardManager.removePrimaryClipChangedListener(clipListener)
        super.onDestroy()
    }
}
