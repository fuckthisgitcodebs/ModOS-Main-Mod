package com.mod.os.recents.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.RecentApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentsCardStack(
    recents: List<RecentApp>,
    onCardClick: (RecentApp) -> Unit,
    onCardLongClick: (RecentApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val maxVisible = 6
    val baseScale = 0.94f
    val scaleStep = 0.03f
    val alphaStep = 0.10f
    val offsetStep = 28.dp

    BoxWithConstraints(modifier) {
        val cardWidth = maxWidth * 0.90f
        val cardHeightRatio = 0.55f
        val cardHeight = cardWidth * cardHeightRatio

        recents.take(maxVisible).reversed().forEachIndexed { index, app ->
            val scale = baseScale - (index * scaleStep)
            val alpha = 0.90f - (index * alphaStep)
            val yOffset = (-index * offsetStep.value).dp

            // Load app icon once per packageName, cached by remember key
            val iconPainter = remember(app.packageName) {
                runCatching {
                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                    BitmapPainter(drawable.toBitmap().asImageBitmap())
                }.getOrNull()
            }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = yOffset)
                    .scale(scale)
                    .alpha(alpha)
                    .zIndex(index.toFloat())
                    .size(width = cardWidth, height = cardHeight)
                    .clip(MaterialTheme.shapes.medium)
                    .combinedClickable(
                        onClick = { onCardClick(app) },
                        onLongClick = { onCardLongClick(app) }
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    // Sensitive overlay — blurs entire card content
                    if (app.isSensitive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f))
                        )
                    }

                    // Card body — app icon + clip preview text
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // App icon
                        if (iconPainter != null) {
                            androidx.compose.foundation.Image(
                                painter = iconPainter,
                                contentDescription = "${app.label} icon",
                                modifier = Modifier.size(44.dp)
                            )
                        } else {
                            // Fallback placeholder if icon load fails
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        Color(0xFF2A2A2A),
                                        MaterialTheme.shapes.small
                                    )
                            )
                        }

                        // Text content
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = app.label,
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!app.isSensitive) {
                                Text(
                                    text = app.contentPreview,
                                    color = Color.White.copy(alpha = 0.60f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "Sensitive content",
                                    color = Color(0xFFFF6B6B).copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (app.clipType != ClipType.TEXT_PLAIN) {
                                Text(
                                    text = app.clipType.name.replace("TEXT_", ""),
                                    color = Color(0xFF00D4FF),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
