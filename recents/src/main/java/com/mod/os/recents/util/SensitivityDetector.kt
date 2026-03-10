package com.mod.os.recents.util

import com.mod.os.recents.contract.HostBridge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensitivityDetector @Inject constructor(
    private val hostBridge: HostBridge
) {

    private val sensitivePatterns = listOf(
        Regex("""\b\d{4}[- ]?\d{4}[- ]?\d{4}[- ]?\d{4}\b"""),
        Regex("""\b\d{3}[-.]?\d{2}[-.]?\d{4}\b"""),
        Regex("""\b[A-Z]{2}\d{6,9}\b"""),
        Regex("""password|pass|pw|secret|key|token|api_key|auth""", RegexOption.IGNORE_CASE)
    )

    suspend fun isSensitive(content: String, sourcePackage: String): Boolean {
        val hostDecision = hostBridge.isPackageSensitive(sourcePackage)
        if (hostDecision != null) return hostDecision

        return sensitivePatterns.any { it.containsMatchIn(content) }
    }
}
