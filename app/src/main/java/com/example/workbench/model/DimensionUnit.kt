package com.example.workbench.model

import java.math.BigDecimal

enum class DimensionUnit(val symbol: String, val displayName: String, val toMmFactor: BigDecimal) {
    MM("mm", "Millimeters", BigDecimal("1")),
    CM("cm", "Centimeters", BigDecimal("10")),
    M("m", "Meters", BigDecimal("1000")),
    IN("in", "Inches", BigDecimal("25.4")),
    FT("ft", "Feet", BigDecimal("304.8"));

    companion object {
        fun fromSymbol(symbol: String): DimensionUnit {
            return entries.find { it.symbol.equals(symbol, ignoreCase = true) }
                ?: CM
        }
    }
}
