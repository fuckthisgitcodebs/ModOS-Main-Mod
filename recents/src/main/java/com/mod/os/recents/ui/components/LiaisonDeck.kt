package com.mod.os.recents.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            // FIX: Replaced transparent gradient + RenderEffect blur with a solid
            // dark surface. The blur was compositing against Color.Transparent and
            // rendering the entire panel invisible (pure black output).
            .background(Color(0xFF0D0D0D))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
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
                        colors = ButtonDefaults.buttonColors(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
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
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
