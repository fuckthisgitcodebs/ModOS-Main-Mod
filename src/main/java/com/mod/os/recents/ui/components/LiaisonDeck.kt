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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    .background(
        brush = Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.3f to Color.Black.copy(alpha = 0.7f),
            1.0f to Color.Black
        )
    )
    .graphicsLayer {
        renderEffect = RenderEffect.createBlurEffect(
            radiusX = 24f,
            radiusY = 24f,
            edgeTreatment = ShaderSource.TileMode.CLAMP
        ).asComposeRenderEffect()
    }
    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
