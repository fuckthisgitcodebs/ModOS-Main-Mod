package com.mod.os.recents.clipboard

import com.mod.os.recents.contract.ClipboardObserverCallback
import com.mod.os.recents.contract.HostBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardMonitor @Inject constructor(
    private val hostBridge: HostBridge,
    private val repository: ClipboardRepository
) : ClipboardObserverCallback {

    // FIX: Named job reference so stopMonitoring() can cancel pending coroutines.
    // Previously the scope had no handle — stop was unregister-only, coroutines
    // launched via scope.launch{} would outlive the monitoring session.
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    fun startMonitoring() {
        hostBridge.registerClipboardObserver(this)
    }

    fun stopMonitoring() {
        hostBridge.unregisterClipboardObserver(this)
        job.cancel() // FIX: cancel any in-flight addClipboardContent coroutines
    }

    override fun onClipboardChanged(
        content: String?,
        mimeType: String?,
        sourcePackage: String?,
        sourceLabel: String?
    ) {
        if (content.isNullOrBlank() || sourcePackage.isNullOrBlank()) return
        scope.launch {
            repository.addClipboardContent(
                content = content,
                mimeType = mimeType,
                sourcePackage = sourcePackage,
                sourceLabel = sourceLabel
            )
        }
    }
}
