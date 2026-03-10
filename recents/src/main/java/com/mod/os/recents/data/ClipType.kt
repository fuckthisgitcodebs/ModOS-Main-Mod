package com.mod.os.recents.data

enum class ClipType {
    TEXT_PLAIN,
    TEXT_URL,
    TEXT_EMAIL,
    TEXT_PHONE,
    TEXT_ADDRESS,
    TEXT_HEX_COLOR,
    TEXT_JSON,
    TEXT_MARKDOWN,
    TEXT_CODE,
    TEXT_CODE_BLOCK,
    TEXT_TERMINAL_COMMAND,
    TEXT_AI_PROMPT,
    TEXT_AI_IMAGE_GEN_PROMPT,
    URI_CONTENT,
    IMAGE_URI,
    UNKNOWN;

    companion object {
        fun fromMimeAndContent(mime: String?, content: String): ClipType {
            if (content.isBlank()) return TEXT_PLAIN

            val trimmed = content.trim()
            val lower = trimmed.lowercase()

            if (trimmed.contains("[[ START FILE:", ignoreCase = true) &&
                trimmed.contains("[[ END FILE ]]]", ignoreCase = true)) {
                return TEXT_CODE_BLOCK
            }

            if (
                trimmed.startsWith("$ ") || trimmed.startsWith("# ") || trimmed.startsWith("% ") ||
                trimmed.startsWith("> ") || trimmed.startsWith("(base) ") ||
                lower.matches(Regex("""^\s*(ls|cd|pwd|cat|echo|grep|find|mkdir|rm|mv|cp|touch|nano|vim|vi|pkg|apt|dnf|yum|git|curl|wget|ping|top|htop|neofetch|clear|history|export|source|chmod|chown)\b.*""")) ||
                (lower.contains(Regex("""\s(-[a-z]+|--[a-z-]+)""")) && lower.contains("/")) ||
                lower.contains("~/") || lower.contains("/home/") || lower.contains("/sdcard/") ||
                lower.contains("termux") || lower.contains("proot") || lower.contains("chroot")
            ) {
                return TEXT_TERMINAL_COMMAND
            }

            val imageIndicators = listOf(
                "generate", "create", "draw", "render", "illustrate", "paint", "depict",
                "image", "picture", "photo", "portrait", "artwork", "illustration", "rendering",
                "style of", "in the style", "highly detailed", "masterpiece", "best quality",
                "8k", "4k", "ultra detailed", "cinematic", "photorealistic", "hyperrealistic",
                "concept art", "digital art", "anime", "manga", "oil painting", "watercolor"
            )

            val hasImageKeywords = imageIndicators.any { lower.contains(it) }
            val hasPromptStructure = trimmed.length > 60 &&
                    (lower.contains("a ") || lower.contains("an ") || lower.startsWith("the ") ||
                     lower.contains(",") && lower.contains(" and ") && trimmed.split(" ").size > 12)

            if (hasImageKeywords && hasPromptStructure) {
                return TEXT_AI_IMAGE_GEN_PROMPT
            }

            val aiIndicators = listOf(
                "prompt:", "as an ai", "you are", "act as", "roleplay", "gemini", "claude",
                "chatgpt", "grok", "llm", "language model", "generate text", "write a",
                "explain", "summarize", "translate", "improve", "rewrite"
            )

            if (aiIndicators.any { lower.contains(it) } ||
                (trimmed.length > 100 && trimmed.count { it == ',' || it == '.' } >= 5)) {
                return TEXT_AI_PROMPT
            }

            return when {
                mime?.startsWith("image/") == true -> IMAGE_URI
                mime == "text/uri-list" -> URI_CONTENT
                trimmed.startsWith("http://") || trimmed.startsWith("https://") -> TEXT_URL
                trimmed.contains("@") && trimmed.matches(Regex(".*@[^@]+\\.[^@]+")) -> TEXT_EMAIL
                trimmed.matches(Regex("""^\+?\d{7,15}$""")) -> TEXT_PHONE
                trimmed.matches(Regex("""^#?[0-9a-f]{6}$""", RegexOption.IGNORE_CASE)) -> TEXT_HEX_COLOR
                trimmed.startsWith("{") || trimmed.startsWith("[") -> TEXT_JSON
                trimmed.contains("```") -> TEXT_CODE
                trimmed.contains("# ") && (trimmed.contains("---") || trimmed.contains("***")) -> TEXT_MARKDOWN
                else -> TEXT_PLAIN
            }
        }
    }
}
