package com.example.workbench

import com.example.workbench.calculator.WorkBenchCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class WorkBenchCalculatorTest {

    // 1. Addition (120 cm + 5 cm = 125 cm)
    @Test
    fun testAddition() {
        val a = BigDecimal("120")
        val b = BigDecimal("5")
        val result = WorkBenchCalculator.addDimensions(a, b)
        assertEquals(BigDecimal("125"), result)
    }

    // 2. Subtraction (120 cm − 5 cm = 115 cm)
    @Test
    fun testSubtraction() {
        val a = BigDecimal("120")
        val b = BigDecimal("5")
        val result = WorkBenchCalculator.subtractDimensions(a, b)
        assertEquals(BigDecimal("115"), result)
    }

    // 3. Negative subtraction rejection (100 - 150 rejected)
    @Test
    fun testNegativeSubtractionRejection() {
        val a = BigDecimal("100")
        val b = BigDecimal("150")
        val exception = assertThrows(IllegalArgumentException::class.java) {
            WorkBenchCalculator.subtractDimensions(a, b)
        }
        assertEquals("Result cannot be negative.", exception.message)
    }

    // 4. Half (120 cm ÷ 2 = 60 cm)
    @Test
    fun testHalf() {
        val a = BigDecimal("120")
        val result = WorkBenchCalculator.halfDimension(a)
        assertEquals(BigDecimal("60"), result)
    }

    // 5. Double (120 cm × 2 = 240 cm)
    @Test
    fun testDouble() {
        val a = BigDecimal("120")
        val result = WorkBenchCalculator.doubleDimension(a)
        assertEquals(BigDecimal("240"), result)
    }

    // 6. Piece total (Piece: 30 cm, Qty: 4, Gap: 2 cm -> 30*4 + 2*3 = 126 cm)
    @Test
    fun testPieceTotalMultiplePiecesWithGap() {
        val piece = BigDecimal("30")
        val qty = 4
        val gap = BigDecimal("2")
        val result = WorkBenchCalculator.calculatePieceTotal(piece, qty, gap)
        assertEquals(BigDecimal("126"), result)
    }

    // 7. One piece (Quantity = 1 -> total = pieceSize, no gap added)
    @Test
    fun testOnePieceIgnoresGap() {
        val piece = BigDecimal("50.5")
        val qty = 1
        val gap = BigDecimal("10")
        val result = WorkBenchCalculator.calculatePieceTotal(piece, qty, gap)
        assertEquals(BigDecimal("50.5"), result)
    }

    // 8. Multiple pieces without gap
    @Test
    fun testMultiplePiecesZeroGap() {
        val piece = BigDecimal("25")
        val qty = 3
        val result = WorkBenchCalculator.calculatePieceTotal(piece, qty, BigDecimal.ZERO)
        assertEquals(BigDecimal("75"), result)
    }

    // 9. Gap calculation with varying quantity
    @Test
    fun testGapCalculationFormula() {
        // 5 pieces of 10 with gap of 3 -> 5*10 + 4*3 = 50 + 12 = 62
        val piece = BigDecimal("10")
        val qty = 5
        val gap = BigDecimal("3")
        val result = WorkBenchCalculator.calculatePieceTotal(piece, qty, gap)
        assertEquals(BigDecimal("62"), result)
    }

    // 10. Centering (Available: 120 cm, Object: 80 cm -> Left: 20 cm, Right: 20 cm)
    @Test
    fun testCentering() {
        val available = BigDecimal("120")
        val obj = BigDecimal("80")
        val center = WorkBenchCalculator.calculateCenterPosition(available, obj)
        assertEquals(BigDecimal("20"), center.left)
        assertEquals(BigDecimal("20"), center.right)
        assertEquals(BigDecimal("40"), center.remaining)
    }

    // 11. Equal left/right spacing
    @Test
    fun testCenteringEqualSpacing() {
        val available = BigDecimal("95.5")
        val obj = BigDecimal("45.5")
        val center = WorkBenchCalculator.calculateCenterPosition(available, obj)
        assertEquals(center.left, center.right)
        assertEquals(BigDecimal("25"), center.left)
    }

    // 11b. Centering rejection when object > available
    @Test
    fun testCenteringObjectLargerThanAvailableRejection() {
        val available = BigDecimal("100")
        val obj = BigDecimal("120")
        val exception = assertThrows(IllegalArgumentException::class.java) {
            WorkBenchCalculator.calculateCenterPosition(available, obj)
        }
        assertEquals("Object is larger than available space.", exception.message)
    }

    // 12. Area (180 cm × 60 cm = 1.08 m²)
    @Test
    fun testAreaCalculation() {
        // 180 cm = 1800 mm, 60 cm = 600 mm
        val lengthMm = BigDecimal("1800")
        val widthMm = BigDecimal("600")
        val areaM2 = WorkBenchCalculator.calculateAreaInSquareMeters(lengthMm, widthMm)
        assertEquals(BigDecimal("1.08"), areaM2)
    }

    // 13. Volume (180 cm × 60 cm × 85 cm = 0.918 m³)
    @Test
    fun testVolumeCalculation() {
        // 180 cm = 1800 mm, 60 cm = 600 mm, 85 cm = 850 mm
        val lengthMm = BigDecimal("1800")
        val widthMm = BigDecimal("600")
        val heightMm = BigDecimal("850")
        val volumeM3 = WorkBenchCalculator.calculateVolumeInCubicMeters(lengthMm, widthMm, heightMm)
        assertEquals(BigDecimal("0.918"), volumeM3)
    }

    // 14. Decimal dimensions
    @Test
    fun testDecimalDimensions() {
        val a = BigDecimal("12.375")
        val b = BigDecimal("4.625")
        val sum = WorkBenchCalculator.addDimensions(a, b)
        assertEquals(BigDecimal("17"), sum)
    }

    // 16. Invalid values / negative checks
    @Test
    fun testNegativeInputRejection() {
        assertThrows(IllegalArgumentException::class.java) {
            WorkBenchCalculator.addDimensions(BigDecimal("-5"), BigDecimal("10"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WorkBenchCalculator.calculatePieceTotal(BigDecimal("-10"), 2, BigDecimal("2"))
        }
    }

    // 17. Zero values
    @Test
    fun testZeroValues() {
        val sum = WorkBenchCalculator.addDimensions(BigDecimal("0"), BigDecimal("15"))
        assertEquals(BigDecimal("15"), sum)

        val halfZero = WorkBenchCalculator.halfDimension(BigDecimal("0"))
        assertEquals(BigDecimal("0"), halfZero)
    }

    // 18. BigDecimal precision (no floating-point rounding errors like 0.1 + 0.2 != 0.3)
    @Test
    fun testBigDecimalPrecision() {
        val a = BigDecimal("0.1")
        val b = BigDecimal("0.2")
        val result = WorkBenchCalculator.addDimensions(a, b)
        assertEquals(BigDecimal("0.3"), result)
    }
}
