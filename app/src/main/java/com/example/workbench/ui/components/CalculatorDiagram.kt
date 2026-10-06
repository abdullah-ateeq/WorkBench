package com.example.workbench.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.model.CalculationType
import com.example.workbench.model.DimensionUnit
import java.math.BigDecimal

@Composable
fun CalculatorDiagram(
    mode: CalculationType,
    val1: BigDecimal?,
    val2: BigDecimal?,
    val3: BigDecimal?,
    qty: Int?,
    result: BigDecimal?,
    unit: DimensionUnit,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        when (mode) {
            CalculationType.TOTAL_FROM_PIECES -> {
                drawPiecesDiagram(
                    pieceSize = val1 ?: BigDecimal("30"),
                    qty = qty ?: 4,
                    gap = val3 ?: BigDecimal("2"),
                    total = result ?: BigDecimal("126"),
                    unit = unit,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor,
                    fillColor = surfaceVariant.copy(alpha = 0.5f),
                    textColor = textColor,
                    decimalPlaces = decimalPlaces
                )
            }
            CalculationType.CENTER_POSITION -> {
                drawCenterDiagram(
                    available = val1 ?: BigDecimal("120"),
                    objectSize = val2 ?: BigDecimal("80"),
                    spacing = result ?: BigDecimal("20"),
                    unit = unit,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor,
                    fillColor = surfaceVariant.copy(alpha = 0.5f),
                    textColor = textColor,
                    decimalPlaces = decimalPlaces
                )
            }
            CalculationType.ADD, CalculationType.SUBTRACT -> {
                drawAddSubDiagram(
                    base = val1 ?: BigDecimal("100"),
                    change = val2 ?: BigDecimal("20"),
                    isAdd = mode == CalculationType.ADD,
                    result = result ?: BigDecimal("120"),
                    unit = unit,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor,
                    fillColor = surfaceVariant.copy(alpha = 0.5f),
                    errorColor = errorColor,
                    textColor = textColor,
                    decimalPlaces = decimalPlaces
                )
            }
            CalculationType.HALF, CalculationType.DOUBLE -> {
                drawHalfDoubleDiagram(
                    base = val1 ?: BigDecimal("100"),
                    isHalf = mode == CalculationType.HALF,
                    result = result ?: BigDecimal("50"),
                    unit = unit,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor,
                    fillColor = surfaceVariant.copy(alpha = 0.5f),
                    textColor = textColor,
                    decimalPlaces = decimalPlaces
                )
            }
        }
    }
}

private fun DrawScope.drawPiecesDiagram(
    pieceSize: BigDecimal,
    qty: Int,
    gap: BigDecimal,
    total: BigDecimal,
    unit: DimensionUnit,
    primaryColor: Color,
    outlineColor: Color,
    fillColor: Color,
    textColor: Color,
    decimalPlaces: Int
) {
    val displayQty = qty.coerceIn(1, 8)
    val width = size.width
    val height = size.height

    val topY = 22f
    val barTop = 50f
    val barHeight = 40f
    val marginX = 20f
    val availableWidth = width - 2 * marginX

    val gapRatio = if (displayQty > 1) 0.12f else 0f
    val totalUnits = displayQty + (displayQty - 1) * gapRatio
    val pieceWidth = availableWidth / totalUnits
    val gapWidth = pieceWidth * gapRatio

    var currentX = marginX
    for (i in 0 until displayQty) {
        drawRect(fillColor, Offset(currentX, barTop), Size(pieceWidth, barHeight))
        drawRect(outlineColor, Offset(currentX, barTop), Size(pieceWidth, barHeight), style = Stroke(1.5.dp.toPx()))

        if (i < displayQty - 1 && gapWidth > 2f) {
            val gapStartX = currentX + pieceWidth
            drawLine(
                color = primaryColor.copy(alpha = 0.5f),
                start = Offset(gapStartX, barTop + barHeight / 2),
                end = Offset(gapStartX + gapWidth, barTop + barHeight / 2),
                strokeWidth = 1.dp.toPx()
            )
        }
        currentX += pieceWidth + gapWidth
    }

    // Top Dimension Line (Total)
    drawLine(primaryColor, Offset(marginX, topY), Offset(width - marginX, topY), strokeWidth = 1.5.dp.toPx())
    drawLine(primaryColor, Offset(marginX, topY - 8f), Offset(marginX, topY + 8f), strokeWidth = 1.5.dp.toPx())
    drawLine(primaryColor, Offset(width - marginX, topY - 8f), Offset(width - marginX, topY + 8f), strokeWidth = 1.5.dp.toPx())

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 24f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        val label = "Total: ${DimensionFormatter.formatMeasurement(total, unit, decimalPlaces)} (${displayQty} pcs)"
        drawText(label, width / 2, topY - 6f, paint)
    }
}

private fun DrawScope.drawCenterDiagram(
    available: BigDecimal,
    objectSize: BigDecimal,
    spacing: BigDecimal,
    unit: DimensionUnit,
    primaryColor: Color,
    outlineColor: Color,
    fillColor: Color,
    textColor: Color,
    decimalPlaces: Int
) {
    val width = size.width
    val height = size.height

    val marginX = 24f
    val barTop = 45f
    val barHeight = 44f
    val availableW = width - 2 * marginX

    val objRatio = if (available > BigDecimal.ZERO && objectSize <= available) {
        (objectSize.toFloat() / available.toFloat()).coerceIn(0.1f, 0.95f)
    } else 0.5f

    val objW = availableW * objRatio
    val spaceW = (availableW - objW) / 2f

    // Outer frame (Available space)
    drawRect(outlineColor.copy(alpha = 0.3f), Offset(marginX, barTop), Size(availableW, barHeight), style = Stroke(1.5.dp.toPx()))

    // Inner object (Centered)
    val objX = marginX + spaceW
    drawRect(fillColor, Offset(objX, barTop + 4f), Size(objW, barHeight - 8f))
    drawRect(primaryColor, Offset(objX, barTop + 4f), Size(objW, barHeight - 8f), style = Stroke(2.dp.toPx()))

    // Top indicator arrows for spacing
    val lineY = 22f
    // Left space line
    drawLine(primaryColor, Offset(marginX, lineY), Offset(objX, lineY), strokeWidth = 1.5.dp.toPx())
    drawLine(primaryColor, Offset(marginX, lineY - 6f), Offset(marginX, lineY + 6f), strokeWidth = 1.5.dp.toPx())
    drawLine(primaryColor, Offset(objX, lineY - 6f), Offset(objX, lineY + 6f), strokeWidth = 1.5.dp.toPx())

    // Right space line
    val rightObjX = objX + objW
    drawLine(primaryColor, Offset(rightObjX, lineY), Offset(width - marginX, lineY), strokeWidth = 1.5.dp.toPx())
    drawLine(primaryColor, Offset(rightObjX, lineY - 6f), Offset(rightObjX, lineY + 6f), strokeWidth = 1.5.dp.toPx())
    drawLine(primaryColor, Offset(width - marginX, lineY - 6f), Offset(width - marginX, lineY + 6f), strokeWidth = 1.5.dp.toPx())

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 22f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        val spLabel = DimensionFormatter.formatMeasurement(spacing, unit, decimalPlaces)
        drawText(spLabel, marginX + spaceW / 2, lineY - 6f, paint)
        drawText(spLabel, rightObjX + spaceW / 2, lineY - 6f, paint)
        val objLabel = "Object: ${DimensionFormatter.formatMeasurement(objectSize, unit, decimalPlaces)}"
        drawText(objLabel, objX + objW / 2, barTop + barHeight / 2 + 7f, paint)
    }
}

private fun DrawScope.drawAddSubDiagram(
    base: BigDecimal,
    change: BigDecimal,
    isAdd: Boolean,
    result: BigDecimal,
    unit: DimensionUnit,
    primaryColor: Color,
    outlineColor: Color,
    fillColor: Color,
    errorColor: Color,
    textColor: Color,
    decimalPlaces: Int
) {
    val width = size.width
    val marginX = 24f
    val barTop = 45f
    val barHeight = 36f
    val availableW = width - 2 * marginX

    val baseRatio = 0.65f
    val changeRatio = 0.25f
    val baseW = availableW * baseRatio
    val changeW = availableW * changeRatio

    drawRect(fillColor, Offset(marginX, barTop), Size(baseW, barHeight))
    drawRect(outlineColor, Offset(marginX, barTop), Size(baseW, barHeight), style = Stroke(1.5.dp.toPx()))

    val changeX = if (isAdd) marginX + baseW else marginX + baseW - changeW
    val changeColor = if (isAdd) primaryColor.copy(alpha = 0.3f) else errorColor.copy(alpha = 0.2f)
    drawRect(changeColor, Offset(changeX, barTop), Size(changeW, barHeight))
    drawRect(primaryColor, Offset(changeX, barTop), Size(changeW, barHeight), style = Stroke(1.5.dp.toPx()))

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 22f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        val label = if (isAdd) "+ ${DimensionFormatter.formatMeasurement(change, unit, decimalPlaces)}"
        else "− ${DimensionFormatter.formatMeasurement(change, unit, decimalPlaces)}"
        drawText(label, changeX + changeW / 2, barTop + barHeight / 2 + 7f, paint)
    }
}

private fun DrawScope.drawHalfDoubleDiagram(
    base: BigDecimal,
    isHalf: Boolean,
    result: BigDecimal,
    unit: DimensionUnit,
    primaryColor: Color,
    outlineColor: Color,
    fillColor: Color,
    textColor: Color,
    decimalPlaces: Int
) {
    val width = size.width
    val marginX = 24f
    val barTop = 45f
    val barHeight = 34f
    val availableW = width - 2 * marginX

    val fullW = availableW * 0.85f
    val splitW = if (isHalf) fullW / 2f else fullW

    drawRect(fillColor, Offset(marginX, barTop), Size(splitW, barHeight))
    drawRect(primaryColor, Offset(marginX, barTop), Size(splitW, barHeight), style = Stroke(2.dp.toPx()))

    if (isHalf) {
        drawRect(outlineColor.copy(alpha = 0.3f), Offset(marginX + splitW, barTop), Size(splitW, barHeight), style = Stroke(1.5.dp.toPx()))
    }

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = 22f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        val resLabel = DimensionFormatter.formatMeasurement(result, unit, decimalPlaces)
        drawText(resLabel, marginX + splitW / 2, barTop + barHeight / 2 + 7f, paint)
    }
}
