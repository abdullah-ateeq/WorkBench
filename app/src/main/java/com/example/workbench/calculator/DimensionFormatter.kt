package com.example.workbench.calculator

import com.example.workbench.model.DimensionUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object DimensionFormatter {

    fun parseBigDecimal(input: String?): BigDecimal? {
        if (input.isNullOrBlank()) return null
        val clean = input.trim().replace(',', '.')
        return try {
            val bd = BigDecimal(clean)
            if (bd < BigDecimal.ZERO) null else bd
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun formatNumber(value: BigDecimal?, decimalPlaces: Int = 2): String {
        if (value == null) return "0"
        val clampedPlaces = decimalPlaces.coerceIn(0, 4)
        val rounded = value.setScale(clampedPlaces, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (clampedPlaces == 0) {
            "#,##0"
        } else {
            "#,##0." + "0".repeat(clampedPlaces)
        }
        val df = DecimalFormat(pattern, symbols)
        return df.format(rounded)
    }

    fun formatNumberTrimZeros(value: BigDecimal?, maxDecimalPlaces: Int = 2): String {
        if (value == null) return "0"
        val clampedPlaces = maxDecimalPlaces.coerceIn(0, 4)
        val rounded = value.setScale(clampedPlaces, RoundingMode.HALF_UP).stripTrailingZeros()
        // If it's zero or integer scale
        if (rounded.scale() <= 0) {
            val symbols = DecimalFormatSymbols(Locale.US)
            return DecimalFormat("#,##0", symbols).format(rounded)
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0." + "#".repeat(clampedPlaces), symbols)
        return df.format(rounded)
    }

    fun formatMeasurement(
        value: BigDecimal?,
        unit: DimensionUnit,
        decimalPlaces: Int = 2
    ): String {
        if (value == null) return "—"
        val formatted = formatNumber(value, decimalPlaces)
        return "$formatted ${unit.symbol}"
    }

    fun formatArea(areaM2: BigDecimal?, decimalPlaces: Int = 2): String {
        if (areaM2 == null) return "—"
        val formatted = formatNumber(areaM2, decimalPlaces)
        return "$formatted m²"
    }

    fun formatVolume(volumeM3: BigDecimal?, decimalPlaces: Int = 3): String {
        if (volumeM3 == null) return "—"
        val formatted = formatNumber(volumeM3, decimalPlaces)
        return "$formatted m³"
    }

    fun formatSummaryDimensions(
        length: BigDecimal?,
        width: BigDecimal?,
        height: BigDecimal?,
        unit: DimensionUnit,
        decimalPlaces: Int = 2
    ): String {
        val parts = mutableListOf<String>()
        if (length != null) parts.add(formatNumber(length, decimalPlaces))
        if (width != null) parts.add(formatNumber(width, decimalPlaces))
        if (height != null) parts.add(formatNumber(height, decimalPlaces))
        return if (parts.isNotEmpty()) {
            parts.joinToString(" × ") + " ${unit.symbol}"
        } else {
            "—"
        }
    }
}
