package com.example.workbench.calculator

import com.example.workbench.model.DimensionUnit
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object UnitConverter {
    private fun BigDecimal.normalizeDecimal(): BigDecimal {
        val stripped = if (this.scale() > 0) this.stripTrailingZeros() else this
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }

    fun toMillimeters(value: BigDecimal, fromUnit: DimensionUnit): BigDecimal {
        return value.multiply(fromUnit.toMmFactor).normalizeDecimal()
    }

    fun fromMillimeters(valueInMm: BigDecimal, toUnit: DimensionUnit): BigDecimal {
        if (toUnit.toMmFactor.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return valueInMm.divide(toUnit.toMmFactor, 10, RoundingMode.HALF_UP).normalizeDecimal()
    }

    fun convert(value: BigDecimal, fromUnit: DimensionUnit, toUnit: DimensionUnit): BigDecimal {
        if (fromUnit == toUnit) return value
        val inMm = toMillimeters(value, fromUnit)
        return fromMillimeters(inMm, toUnit)
    }
}
