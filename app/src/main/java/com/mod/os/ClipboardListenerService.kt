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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint

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
        Log.d("ModOS_Clip", "OnPrimaryClipChangedListener fired — holder=${AccessibilityServiceHolder.instance}")
        AccessibilityServiceHolder.instance?.onClipboardChangedBySystem()
            ?: Log.w("ModOS_Clip", "holder is null — accessibility service not connected yet")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ModOS_Clip", "ClipboardListenerService onCreate — starting foreground")
        createNotificationChannel()
        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
        clipboardManager.addPrimaryClipChangedListener(listener)
        Log.d("ModOS_Clip", "Listener registered. Foreground active.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ModOS_Clip", "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("ModOS_Clip", "ClipboardListenerService onDestroy")
        clipboardManager.removePrimaryClipChangedListener(listener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Clipboard Monitor",
            NotificationManager.IMPORTANCE_MIN
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
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ModOS")
            .setContentText("Clipboard monitoring active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }
}
