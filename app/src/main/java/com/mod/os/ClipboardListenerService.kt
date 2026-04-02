package com.mod.os

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service that holds the ClipboardManager listener.
 *
 * WHY FOREGROUND: Samsung OneUI 8 Freecess process manager freezes background
 * processes aggressively. A frozen process cannot receive ClipboardManager
 * callbacks. Foreground services are exempted from Freecess freezing.
 * Without this, OnPrimaryClipChangedListener silently dies in the background.
 *
 * The persistent notification is the price of keeping the process alive.
 * Made minimal: no sound, no vibration, low priority, collapsed.
 */
@AndroidEntryPoint
class ClipboardListenerService : Service() {

    companion object {
        const val CHANNEL_ID = "modos_clipboard_monitor"
        const val NOTIF_ID = 1001
    }

    private val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        AccessibilityServiceHolder.instance?.onClipboardChangedBySystem()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Promote to foreground BEFORE doing any work — required on Android 14+
        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onDestroy() {
        clipboardManager.removePrimaryClipChangedListener(listener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Clipboard Monitor",
            NotificationManager.IMPORTANCE_MIN  // No sound, no pop-up, no badge
        ).apply {
            description = "Keeps ModOS clipboard monitoring active"
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ModOS")
            .setContentText("Clipboard monitoring active")
            .setSmallIcon(android.R.drawable.ic_menu_clipboard)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // hidden on lock screen
            .build()
    }
}
