package com.mod.os

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HighlightModeService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private var triggerButtonView: ComposeView? = null
    private var interceptorView: ComposeView? = null
    private var resultSheetView: ComposeView? = null
    private var highlightModeActive = false
    private var pendingResults: List<ScreenTextExtractor.ExtractedNode> = emptyList()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        addTriggerButton()
    }

    private fun baseParams(w: Int, h: Int, flags: Int) = WindowManager.LayoutParams(
        w, h,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        flags,
        PixelFormat.TRANSPARENT
    )

    private fun addTriggerButton() {
        val params = baseParams(
            160, 160,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 300
        }

        var startX = 0; var startY = 0
        var startParamX = 0; var startParamY = 0
        var isDragging = false

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@HighlightModeService)
            setViewTreeSavedStateRegistryOwner(this@HighlightModeService)
            setContent {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF00D4FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✦",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt()
                    startY = event.rawY.toInt()
                    startParamX = params.x
                    startParamY = params.y
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - startX
                    val dy = event.rawY.toInt() - startY
                    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) isDragging = true
                    if (isDragging) {
                        params.x = startParamX + dx
                        params.y = startParamY + dy
                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) activateHighlightMode()
                    true
                }
                else -> false
            }
        }

        windowManager.addView(view, params)
        triggerButtonView = view
    }

    private fun activateHighlightMode() {
        if (highlightModeActive) return
        highlightModeActive = true
        addInterceptorOverlay()
    }

    private fun addInterceptorOverlay() {
        val params = baseParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            // FIX: FLAG_NOT_FOCUSABLE keeps underlying windows in the
            // accessibility window list — critical for node traversal
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@HighlightModeService)
            setViewTreeSavedStateRegistryOwner(this@HighlightModeService)
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF00D4FF).copy(alpha = 0.08f))
                ) {
                    Text(
                        text = "Tap any text to capture it",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 64.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "✕  Cancel",
                        color = Color(0xFF00D4FF),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                            .background(
                                Color.Black.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .clickable { deactivateHighlightMode() }
                    )
                }
            }
        }

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleTap(event.rawX.toInt(), event.rawY.toInt())
                true
            } else false
        }

        windowManager.addView(view, params)
        interceptorView = view
    }

    private fun handleTap(x: Int, y: Int) {
        // FIX: Use windows list filtered by package, NOT rootInActiveWindow.
        // rootInActiveWindow returns the focused window — which is our overlay.
        // We need the background app's window nodes instead.
        val service = AccessibilityServiceHolder.instance
        val windows = service?.windows ?: emptyList()

        val results = ScreenTextExtractor.extractAtPointFromBackground(
            windows = windows,
            ownPackage = packageName,
            x = x,
            y = y
        )

        pendingResults = results
        removeInterceptorOverlay()
        showResultSheet()
    }

    private fun showResultSheet() {
        val params = baseParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        ).apply {
            gravity = Gravity.BOTTOM
        }

        val captured = pendingResults.map { it.text }.distinct()

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@HighlightModeService)
            setViewTreeSavedStateRegistryOwner(this@HighlightModeService)
            setContent {
                Surface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color(0xFF0F0F0F)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (captured.isEmpty())
                                    "No text found"
                                else
                                    "${captured.size} string${if (captured.size > 1) "s" else ""} captured",
                                color = Color(0xFF00D4FF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "✕",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .clickable {
                                        removeResultSheet()
                                        highlightModeActive = false
                                    }
                                    .padding(8.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        if (captured.isEmpty()) {
                            Text(
                                "The accessibility service couldn't read nodes at that location.\n" +
                                "Try tapping directly on text rather than empty space.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 360.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(captured) { text ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color(0xFF1A1A1A),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = text,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Copy",
                                            color = Color(0xFF00D4FF),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier
                                                .clickable {
                                                    val cm = getSystemService(
                                                        Context.CLIPBOARD_SERVICE
                                                    ) as ClipboardManager
                                                    cm.setPrimaryClip(
                                                        ClipData.newPlainText("ModOS", text)
                                                    )
                                                    removeResultSheet()
                                                    highlightModeActive = false
                                                }
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        windowManager.addView(view, params)
        resultSheetView = view
    }

    private fun deactivateHighlightMode() {
        removeInterceptorOverlay()
        highlightModeActive = false
    }

    private fun removeInterceptorOverlay() {
        interceptorView?.let { runCatching { windowManager.removeView(it) } }
        interceptorView = null
    }

    private fun removeResultSheet() {
        resultSheetView?.let { runCatching { windowManager.removeView(it) } }
        resultSheetView = null
    }

    override fun onDestroy() {
        triggerButtonView?.let { runCatching { windowManager.removeView(it) } }
        removeInterceptorOverlay()
        removeResultSheet()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
