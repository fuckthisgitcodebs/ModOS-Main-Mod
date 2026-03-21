package com.mod.os.recents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mod.os.recents.data.ClipEntry
import com.mod.os.recents.data.ClipType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timestampFormatter = DateTimeFormatter
    .ofPattern("MMM d, yyyy  HH:mm:ss")
    .withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipDetailBottomSheet(
    clip: ClipEntry,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F0F),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = clip.appLabel ?: clip.sourcePackage.split(".").last(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00D4FF),
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1A1A2E)
                ) {
                    Text(
                        text = clip.clipType.name.replace("TEXT_", ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0288D1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timestampFormatter.format(clip.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )

            if (clip.isSensitive) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF3A1A1A)
                ) {
                    Text(
                        text = "⚠ Sensitive content",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(16.dp))

            val isCode = clip.clipType in listOf(
                ClipType.TEXT_CODE,
                ClipType.TEXT_CODE_BLOCK,
                ClipType.TEXT_TERMINAL_COMMAND
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isCode) Color(0xFF0A0A14) else Color(0xFF141414),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = clip.fullContent,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Source: ${clip.sourcePackage}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
