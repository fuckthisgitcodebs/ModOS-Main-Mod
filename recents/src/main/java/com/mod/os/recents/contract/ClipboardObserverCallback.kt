package com.mod.os.recents.contract
interface ClipboardObserverCallback {
fun onClipboardChanged(
content: String?,
mimeType: String?,
sourcePackage: String?,
sourceLabel: String?
)
}
