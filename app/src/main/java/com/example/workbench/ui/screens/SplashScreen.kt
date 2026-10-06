package com.example.workbench.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
            )
        }
        delay(400)
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        delay(900)
        onSplashFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Workbench Outline & Dimension Lines
            Canvas(
                modifier = Modifier
                    .size(240.dp, 160.dp)
            ) {
                val p = progress.value
                val w = size.width
                val h = size.height

                // Bench coordinates
                val topY = h * 0.35f
                val topH = 16f
                val benchW = w * 0.8f
                val startX = (w - benchW) / 2f
                val endX = startX + benchW * p.coerceIn(0f, 1f)

                // 1. Table Top
                if (p > 0.1f) {
                    val topProgress = ((p - 0.1f) / 0.5f).coerceIn(0f, 1f)
                    drawRect(
                        color = primaryColor.copy(alpha = 0.2f),
                        topLeft = Offset(startX, topY),
                        size = Size(benchW * topProgress, topH)
                    )
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(startX, topY),
                        size = Size(benchW * topProgress, topH),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 2. Table Legs
                if (p > 0.3f) {
                    val legProgress = ((p - 0.3f) / 0.5f).coerceIn(0f, 1f)
                    val legH = (h * 0.45f) * legProgress
                    val legW = 12f

                    // Left leg
                    drawRect(
                        color = outlineColor,
                        topLeft = Offset(startX + 16f, topY + topH),
                        size = Size(legW, legH),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Right leg
                    if (p > 0.5f) {
                        drawRect(
                            color = outlineColor,
                            topLeft = Offset(startX + benchW - 28f, topY + topH),
                            size = Size(legW, legH),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    // Bottom stretcher
                    if (p > 0.6f) {
                        val stretchProgress = ((p - 0.6f) / 0.3f).coerceIn(0f, 1f)
                        val stretchY = topY + topH + (h * 0.45f) * 0.6f
                        drawLine(
                            color = outlineColor,
                            start = Offset(startX + 16f + legW, stretchY),
                            end = Offset(startX + 16f + legW + (benchW - 44f - legW) * stretchProgress, stretchY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // 3. Measurement Lines & Dimension Labels
                if (p > 0.5f) {
                    val lineProgress = ((p - 0.5f) / 0.5f).coerceIn(0f, 1f)
                    val dimLineY = topY - 18f

                    // Length dimension line
                    drawLine(
                        color = primaryColor,
                        start = Offset(startX, dimLineY),
                        end = Offset(startX + benchW * lineProgress, dimLineY),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Left and right ticks
                    drawLine(
                        color = primaryColor,
                        start = Offset(startX, dimLineY - 6f),
                        end = Offset(startX, dimLineY + 6f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    if (lineProgress >= 0.99f) {
                        drawLine(
                            color = primaryColor,
                            start = Offset(startX + benchW, dimLineY - 6f),
                            end = Offset(startX + benchW, dimLineY + 6f),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    // Height dimension line on right
                    if (p > 0.7f) {
                        val hProgress = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)
                        val hLineX = startX + benchW + 16f
                        val hBottom = topY + topH + (h * 0.45f)
                        drawLine(
                            color = primaryColor,
                            start = Offset(hLineX, topY),
                            end = Offset(hLineX, topY + (hBottom - topY) * hProgress),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawLine(
                            color = primaryColor,
                            start = Offset(hLineX - 6f, topY),
                            end = Offset(hLineX + 6f, topY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                // Labels drawn with nativeCanvas
                if (p > 0.75f) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 22f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                        }
                        drawText("L: 180 cm", w / 2f, topY - 24f, paint)

                        paint.textAlign = android.graphics.Paint.Align.LEFT
                        drawText("H: 85 cm", startX + benchW + 24f, topY + (h * 0.45f) / 2f + 10f, paint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name & Subtitle
            Text(
                text = "WORKBENCH",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Store and calculate workshop dimensions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(titleAlpha.value)
            )
        }
    }
}
