package com.example.workbench.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.model.DimensionUnit
import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DimensionDiagram(
    length: BigDecimal?,
    width: BigDecimal?,
    height: BigDecimal?,
    thickness: BigDecimal?,
    unit: DimensionUnit,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    val lLabel = length?.let { "L: ${DimensionFormatter.formatMeasurement(it, unit, decimalPlaces)}" }
    val dLabel = width?.let { "D: ${DimensionFormatter.formatMeasurement(it, unit, decimalPlaces)}" }
    val hLabel = height?.let { "H: ${DimensionFormatter.formatMeasurement(it, unit, decimalPlaces)}" }
    val tLabel = thickness?.let { "T: ${DimensionFormatter.formatMeasurement(it, unit, decimalPlaces)}" }

    val accessibilityDesc = listOfNotNull(
        lLabel?.replace("L:", "Length:"),
        dLabel?.replace("D:", "Depth:"),
        hLabel?.replace("H:", "Height:"),
        tLabel?.replace("T:", "Thickness:")
    ).joinToString(", ")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .semantics { contentDescription = "Technical dimension visualization: $accessibilityDesc" }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (length != null && width != null && height != null) {
            // 3D Isometric technical projection
            drawIsometricBox(
                length = length.toFloat(),
                depth = width.toFloat(),
                height = height.toFloat(),
                thickness = thickness?.toFloat(),
                lengthText = lLabel ?: "",
                depthText = dLabel ?: "",
                heightText = hLabel ?: "",
                thicknessText = tLabel,
                primaryColor = primaryColor,
                outlineColor = outlineColor,
                fillColor = surfaceVariant.copy(alpha = 0.35f),
                textColor = textColor
            )
        } else if (length != null && (width != null || height != null)) {
            // 2D Technical panel projection
            val hVal = (height ?: width)?.toFloat() ?: 50f
            val label2 = hLabel ?: dLabel ?: ""
            draw2dPlate(
                length = length.toFloat(),
                heightOrDepth = hVal,
                lengthText = lLabel ?: "",
                otherText = label2,
                thicknessText = tLabel,
                primaryColor = primaryColor,
                outlineColor = outlineColor,
                fillColor = surfaceVariant.copy(alpha = 0.35f),
                textColor = textColor
            )
        } else {
            // Fallback simple line
            val lVal = (length ?: width ?: height)?.toFloat() ?: 100f
            val singleLabel = lLabel ?: dLabel ?: hLabel ?: ""
            drawSingleDimensionLine(
                lengthText = singleLabel,
                primaryColor = primaryColor,
                textColor = textColor
            )
        }
    }
}

private fun DrawScope.drawIsometricBox(
    length: Float,
    depth: Float,
    height: Float,
    thickness: Float?,
    lengthText: String,
    depthText: String,
    heightText: String,
    thicknessText: String?,
    primaryColor: Color,
    outlineColor: Color,
    fillColor: Color,
    textColor: Color
) {
    val maxDim = maxOf(length, depth, height, 1f)
    // Normalized sizes clamped for clean visual representation
    val boxWidth = (size.width * 0.42f) * (0.6f + 0.4f * (length / maxDim).coerceIn(0.2f, 1f))
    val boxHeight = (size.height * 0.38f) * (0.6f + 0.4f * (height / maxDim).coerceIn(0.2f, 1f))
    val isoAngle = Math.toRadians(30.0)
    val depthScale = (size.width * 0.22f) * (0.6f + 0.4f * (depth / maxDim).coerceIn(0.2f, 1f))

    val dx = (depthScale * cos(isoAngle)).toFloat()
    val dy = (depthScale * sin(isoAngle)).toFloat()

    val centerX = size.width * 0.45f
    val centerY = size.height * 0.58f

    // Front face coordinates:
    // P0: bottom-left, P1: bottom-right, P2: top-right, P3: top-left
    val p0 = Offset(centerX - boxWidth / 2, centerY + boxHeight / 2)
    val p1 = Offset(centerX + boxWidth / 2, centerY + boxHeight / 2)
    val p2 = Offset(centerX + boxWidth / 2, centerY - boxHeight / 2)
    val p3 = Offset(centerX - boxWidth / 2, centerY - boxHeight / 2)

    // Back face shifted by (dx, -dy)
    val p2b = Offset(p2.x + dx, p2.y - dy)
    val p3b = Offset(p3.x + dx, p3.y - dy)
    val p1b = Offset(p1.x + dx, p1.y - dy)

    // Top face Path
    val topFace = Path().apply {
        moveTo(p3.x, p3.y)
        lineTo(p2.x, p2.y)
        lineTo(p2b.x, p2b.y)
        lineTo(p3b.x, p3b.y)
        close()
    }

    // Right face Path
    val rightFace = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p2b.x, p2b.y)
        lineTo(p1b.x, p1b.y)
        close()
    }

    // Front face Path
    val frontFace = Path().apply {
        moveTo(p0.x, p0.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }

    // Fill surfaces
    drawPath(topFace, fillColor)
    drawPath(rightFace, fillColor.copy(alpha = fillColor.alpha * 0.7f))
    drawPath(frontFace, fillColor.copy(alpha = fillColor.alpha * 1.2f))

    // Stroke outlines
    val strokeWidth = 2.dp.toPx()
    val thinStroke = 1.dp.toPx()

    drawPath(frontFace, outlineColor, style = Stroke(width = strokeWidth))
    drawPath(topFace, outlineColor, style = Stroke(width = strokeWidth))
    drawPath(rightFace, outlineColor, style = Stroke(width = strokeWidth))

    // Dimension Line 1: Length (along front top or top-back)
    val lOffset = 18.dp.toPx()
    val lStart = Offset(p3.x, p3.y - lOffset)
    val lEnd = Offset(p2.x, p2.y - lOffset)
    drawDimensionLine(lStart, lEnd, primaryColor, thinStroke)
    // Ticks
    drawLine(primaryColor, Offset(p3.x, p3.y - lOffset - 4f), Offset(p3.x, p3.y - 2f), thinStroke)
    drawLine(primaryColor, Offset(p2.x, p2.y - lOffset - 4f), Offset(p2.x, p2.y - 2f), thinStroke)

    // Dimension Line 2: Height (right side of front face)
    val hOffset = 18.dp.toPx()
    val hStart = Offset(p1b.x + hOffset, p1b.y)
    val hEnd = Offset(p2b.x + hOffset, p2b.y)
    drawDimensionLine(hStart, hEnd, primaryColor, thinStroke)
    drawLine(primaryColor, Offset(p1b.x + 2f, p1b.y), Offset(p1b.x + hOffset + 4f, p1b.y), thinStroke)
    drawLine(primaryColor, Offset(p2b.x + 2f, p2b.y), Offset(p2b.x + hOffset + 4f, p2b.y), thinStroke)

    // Dimension Line 3: Depth (along right bottom angle)
    val dOffset = 16.dp.toPx()
    val dStart = Offset(p1.x + dOffset, p1.y + dOffset / 2)
    val dEnd = Offset(p1b.x + dOffset, p1b.y + dOffset / 2)
    drawDimensionLine(dStart, dEnd, primaryColor, thinStroke)

    // Labels with Android native paint for crisp text
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 28f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }

        // Length text (centered above length line)
        drawText(lengthText, (lStart.x + lEnd.x) / 2, lStart.y - 8f, paint)

        // Height text
        paint.textAlign = android.graphics.Paint.Align.LEFT
        drawText(heightText, hStart.x + 10f, (hStart.y + hEnd.y) / 2 + 10f, paint)

        // Depth text
        paint.textAlign = android.graphics.Paint.Align.CENTER
        drawText(depthText, (dStart.x + dEnd.x) / 2 + 15f, (dStart.y + dEnd.y) / 2 + 25f, paint)

        // Optional thickness
        if (!thicknessText.isNullOrBlank()) {
            paint.textAlign = android.graphics.Paint.Align.LEFT
            paint.textSize = 24f
            drawText(thicknessText, 16f, size.height - 12f, paint)
        }
    }
}

private fun DrawScope.draw2dPlate(
    length: Float,
    heightOrDepth: Float,
    lengthText: String,
    otherText: String,
    thicknessText: String?,
    primaryColor: Color,
    outlineColor: Color,
    fillColor: Color,
    textColor: Color
) {
    val maxDim = maxOf(length, heightOrDepth, 1f)
    val plateW = (size.width * 0.65f) * (0.5f + 0.5f * (length / maxDim).coerceIn(0.2f, 1f))
    val plateH = (size.height * 0.5f) * (0.5f + 0.5f * (heightOrDepth / maxDim).coerceIn(0.2f, 1f))

    val left = (size.width - plateW) / 2f
    val top = (size.height - plateH) / 2f + 10f
    val right = left + plateW
    val bottom = top + plateH

    // Plate rectangle
    drawRect(fillColor, Offset(left, top), androidx.compose.ui.geometry.Size(plateW, plateH))
    drawRect(outlineColor, Offset(left, top), androidx.compose.ui.geometry.Size(plateW, plateH), style = Stroke(2.dp.toPx()))

    // Top dimension line
    val topOffset = 18.dp.toPx()
    val lStart = Offset(left, top - topOffset)
    val lEnd = Offset(right, top - topOffset)
    drawDimensionLine(lStart, lEnd, primaryColor, 1.dp.toPx())
    drawLine(primaryColor, Offset(left, top - topOffset - 4f), Offset(left, top - 2f), 1.dp.toPx())
    drawLine(primaryColor, Offset(right, top - topOffset - 4f), Offset(right, top - 2f), 1.dp.toPx())

    // Right dimension line
    val rightOffset = 18.dp.toPx()
    val rStart = Offset(right + rightOffset, top)
    val rEnd = Offset(right + rightOffset, bottom)
    drawDimensionLine(rStart, rEnd, primaryColor, 1.dp.toPx())
    drawLine(primaryColor, Offset(right + 2f, top), Offset(right + rightOffset + 4f, top), 1.dp.toPx())
    drawLine(primaryColor, Offset(right + 2f, bottom), Offset(right + rightOffset + 4f, bottom), 1.dp.toPx())

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 28f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        drawText(lengthText, (left + right) / 2, top - topOffset - 8f, paint)

        paint.textAlign = android.graphics.Paint.Align.LEFT
        drawText(otherText, right + rightOffset + 8f, (top + bottom) / 2 + 10f, paint)

        if (!thicknessText.isNullOrBlank()) {
            paint.textAlign = android.graphics.Paint.Align.LEFT
            paint.textSize = 24f
            drawText(thicknessText, 16f, size.height - 12f, paint)
        }
    }
}

private fun DrawScope.drawSingleDimensionLine(
    lengthText: String,
    primaryColor: Color,
    textColor: Color
) {
    val y = size.height / 2f
    val startX = size.width * 0.15f
    val endX = size.width * 0.85f

    drawDimensionLine(Offset(startX, y), Offset(endX, y), primaryColor, 2.dp.toPx())
    drawLine(primaryColor, Offset(startX, y - 15f), Offset(startX, y + 15f), 2.dp.toPx())
    drawLine(primaryColor, Offset(endX, y - 15f), Offset(endX, y + 15f), 2.dp.toPx())

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 28f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        drawText(lengthText, (startX + endX) / 2f, y - 15f, paint)
    }
}

private fun DrawScope.drawDimensionLine(start: Offset, end: Offset, color: Color, strokeWidth: Float) {
    drawLine(color, start, end, strokeWidth = strokeWidth, cap = StrokeCap.Round)
}
