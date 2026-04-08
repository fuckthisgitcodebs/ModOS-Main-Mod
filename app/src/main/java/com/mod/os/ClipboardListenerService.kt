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
import com.mod.os.recents.clipboard.ClipboardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardListenerService : Service() {

    companion object {
        const val CHANNEL_ID = "modos_clipboard_monitor"
        const val NOTIF_ID = 1001
    }

    @Inject lateinit var repository: ClipboardRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private var lastForegroundPackage: String? = null
    private var lastForegroundLabel: String? = null
    private var lastClip: String? = null

    fun updateForegroundApp(pkg: String, label: String?) {
        lastForegroundPackage = pkg
        lastForegroundLabel = label
    }

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = try {
            clipboardManager.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(applicationContext)
                ?.toString()
                ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e("ModOS_Clip", "Exception reading clipboard: ${e.message}")
            null
        }

        Log.d("ModOS_Clip", "Read: ${if (clip != null) "SUCCESS — ${clip.take(40)}" else "NULL"}")

        if (clip != null && clip != lastClip) {
            lastClip = clip
            val pkg = lastForegroundPackage ?: "unknown"
            val label = lastForegroundLabel ?: "Clipboard"
            scope.launch {
                repository.addClipboardContent(
                    content = clip,
                    mimeType = "text/plain",
                    sourcePackage = pkg,
                    sourceLabel = label
                )
                Log.d("ModOS_Clip", "Stored from $pkg")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ModOS_Clip", "onCreate — starting foreground")
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
        ClipboardListenerServiceHolder.instance = this
        Log.d("ModOS_Clip", "Listener registered.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        ClipboardListenerServiceHolder.instance = null
        clipboardManager.removePrimaryClipChangedListener(listener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Clipboard Monitor", NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Keeps ModOS clipboard monitoring active"
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
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

object ClipboardListenerServiceHolder {
    var instance: ClipboardListenerService? = null
}
