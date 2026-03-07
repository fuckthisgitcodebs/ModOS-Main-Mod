package com.mod.os.recents.util

import com.mod.os.recents.contract.HostBridge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensitivityDetector @Inject constructor(
    private val hostBridge: HostBridge
) {

    private val sensitivePatterns = listOf(
        Regex("""\b\d{4}[- ]?\d{4}[- ]?\d{4}[- ]?\d{4}\b"""), // credit card
        Regex("""\b\d{3}[-.]?\d{2}[-.]?\d{4}\b"""),          // SSN
        Regex("""\b[A-Z]{2}\d{6,9}\b"""),                     // passport-like
        Regex("""password|pass|pw|secret|key|token|api_key|auth""", RegexOption.IGNORE_CASE)
    )

    suspend fun isSensitive(content: String, sourcePackage: String): Boolean {
        // Host override takes precedence
        val hostSensitive = hostBridge.isPackageSensitive(sourcePackage) // assume future bridge extension
        if (hostSensitive != null) return hostSensitive

        // Pattern-based fallback
        return sensitivePatterns.any { it.containsMatchIn(content) }
    }
}

// Future extension on HostBridge (to be added later):
// fun isPackageSensitive(packageName: String): Boolean?
