package com.mod.os.recents.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.mod.os.recents.data.ClipType
import com.mod.os.recents.ui.RecentApp

@Composable
fun RecentsCardStack(
    recents: List<RecentApp>,
    onCardClick: (RecentApp) -> Unit,
    modifier: Modifier = Modifier
) {
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

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = yOffset)
                    .scale(scale)
                    .alpha(alpha)
                    .zIndex(index.toFloat())
                    .size(width = cardWidth, height = cardHeight)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onCardClick(app) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box {
                    AsyncImage(
                        model = app.previewUri ?: "https://via.placeholder.com/512x280?text=${app.label}",
                        contentDescription = "${app.label} preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    if (app.isSensitive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = app.label,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

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
