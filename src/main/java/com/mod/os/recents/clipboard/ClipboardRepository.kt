package com.mod.os.recents.clipboard
import android.content.SharedPreferences
import com.mod.os.recents.contract.HostBridge
import com.mod.os.recents.data.ClipDao
import com.mod.os.recents.data.ClipEntry
import com.mod.os.recents.data.ClipType
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ClipboardRepository @Inject constructor(
private val clipDao: ClipDao,
private val hostBridge: HostBridge
) {
private val prefs: SharedPreferences
get() = hostBridge.getSharedPreferences()
private val powerAppsKey = "recents_power_apps_packages"
fun getPowerApps(): Set<String> {
return prefs.getStringSet(powerAppsKey, emptySet()) ?: emptySet()
}
suspend fun setPowerApps(packages: Set<String>) {
prefs.edit().putStringSet(powerAppsKey, packages).apply()
}
fun observeActiveClips(): Flow<List<ClipEntry>> {
return clipDao.getActiveClips(50)
}
fun observeActiveClipsForPackage(packageName: String): Flow<List<ClipEntry>> {
return clipDao.observeActiveClipsForPackage(packageName)
}
suspend fun addClipboardContent(
content: String,
mimeType: String?,
sourcePackage: String,
sourceLabel: String?,
isSensitive: Boolean = false
) {
val trimmed = content.trim()
if (trimmed.isEmpty()) return
val hash = computeSha256(trimmed.lowercase())
if (clipDao.findByHash(hash) != null) return
val type = ClipType.fromMimeAndContent(mimeType, trimmed)
val preview = if (trimmed.length > 200) {
trimmed.take(197) + "..."
} else {
trimmed
}
val entry = ClipEntry(
timestamp = Instant.now(),
sourcePackage = sourcePackage,
appLabel = sourceLabel,
contentPreview = preview,
fullContent = trimmed,
mimeType = mimeType,
hash = hash,
clipType = type,
isSensitive = isSensitive,
isArchived = false,
tags = null
)
val insertedId = clipDao.insertClip(entry)
if (insertedId == -1L) return
enforceRetentionRules(sourcePackage)
}
private suspend fun enforceRetentionRules(sourcePackage: String) {
val powerApps = getPowerApps()
if (powerApps.contains(sourcePackage)) {
clipDao.trimActiveTo(30)
clipDao.archiveExcessPowerAppClips(
powerApps = powerApps,
threshold = Instant.now().minusSeconds(30L * 24 * 3600)
)
} else {
clipDao.trimActiveTo(10)
}
clipDao.deleteNonPowerAppArchive(powerApps)
}
private fun computeSha256(input: String): String {
val digest = MessageDigest.getInstance("SHA-256")
val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
return bytes.joinToString("") { "%02x".format(it) }
}
}
