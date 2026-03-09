package com.mod.os.recents.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.BlurEffect
import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.ClipboardPreview

@Composable
fun LiaisonDeck(
    clipboardPreview: ClipboardPreview?,
    detectedType: ClipType?,
    suggestedActions: List<ActionItem>,
    onActionSelected: (ActionItem) -> Unit,
    accentPrimary: Color,
    accentSecondary: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f),
                        Color.Black.copy(alpha = 0.95f)
                    )
                )
            )
            .graphicsLayer {
                renderEffect = BlurEffect(20f, 20f).asComposeRenderEffect()
            }
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp)
    ) {
        clipboardPreview?.let { preview ->
            ClipboardPreviewCard(preview = preview)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (suggestedActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                suggestedActions.forEach { action ->
                    Button(
                        onClick = { onActionSelected(action) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = accentPrimary.copy(alpha = 0.85f)
                        )
                    ) {
                        Text(action.label)
                    }
                }
            }
        } else if (detectedType != null && detectedType != ClipType.TEXT_PLAIN) {
            Text(
                text = "Detected: ${detectedType.name.replace("TEXT_", "")}",
                color = accentSecondary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Module docking area (mini-mods & bridges)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ClipboardPreviewCard(
    preview: ClipboardPreview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = preview.sourceApp,
                color = Color(0xFF00D4FF),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview.text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
