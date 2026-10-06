package com.example.workbench

import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.calculator.UnitConverter
import com.example.workbench.model.DimensionUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class UnitConverterTest {

    // 15. Unit conversion: 180 cm -> 1.8 m, 1800 mm, inches, feet
    @Test
    fun testCentimetersToMeters() {
        val cm = BigDecimal("180")
        val meters = UnitConverter.convert(cm, DimensionUnit.CM, DimensionUnit.M)
        assertEquals(BigDecimal("1.8"), meters)
    }

    @Test
    fun testCentimetersToMillimeters() {
        val cm = BigDecimal("180")
        val mm = UnitConverter.convert(cm, DimensionUnit.CM, DimensionUnit.MM)
        assertEquals(BigDecimal("1800"), mm)
    }

    @Test
    fun testInchesToMillimeters() {
        val inches = BigDecimal("10")
        val mm = UnitConverter.convert(inches, DimensionUnit.IN, DimensionUnit.MM)
        assertEquals(BigDecimal("254"), mm)
    }

    @Test
    fun testFeetToMillimeters() {
        val feet = BigDecimal("2")
        val mm = UnitConverter.convert(feet, DimensionUnit.FT, DimensionUnit.MM)
        assertEquals(BigDecimal("609.6"), mm)
    }

    @Test
    fun testMillimetersToInches() {
        val mm = BigDecimal("25.4")
        val inches = UnitConverter.convert(mm, DimensionUnit.MM, DimensionUnit.IN)
        assertEquals(BigDecimal("1"), inches)
    }

    @Test
    fun testSameUnitConversionReturnsSelf() {
        val v = BigDecimal("42.5")
        val res = UnitConverter.convert(v, DimensionUnit.CM, DimensionUnit.CM)
        assertEquals(v, res)
    }

    @Test
    fun testDimensionFormatterSafeParsing() {
        assertEquals(BigDecimal("120"), DimensionFormatter.parseBigDecimal("120"))
        assertEquals(BigDecimal("120.5"), DimensionFormatter.parseBigDecimal("120.5"))
        assertEquals(BigDecimal("120.5"), DimensionFormatter.parseBigDecimal("120,5"))
        assertEquals(null, DimensionFormatter.parseBigDecimal("-10"))
        assertEquals(null, DimensionFormatter.parseBigDecimal("invalid"))
        assertEquals(null, DimensionFormatter.parseBigDecimal(""))
        assertEquals(null, DimensionFormatter.parseBigDecimal(null))
    }

    @Test
    fun testDimensionFormatterOutput() {
        val cm = BigDecimal("120")
        assertEquals("120.00 cm", DimensionFormatter.formatMeasurement(cm, DimensionUnit.CM, 2))
        assertEquals("120 cm", DimensionFormatter.formatMeasurement(cm, DimensionUnit.CM, 0))
    }
}
