package com.example.workbench.calculator

import com.example.workbench.model.CenterResult
import java.math.BigDecimal
import java.math.RoundingMode

object WorkBenchCalculator {

    private val TWO = BigDecimal("2")
    private val MM2_TO_M2 = BigDecimal("1000000") // 1000 * 1000
    private val MM3_TO_M3 = BigDecimal("1000000000") // 1000 * 1000 * 1000

    private fun BigDecimal.normalizeDecimal(): BigDecimal {
        val stripped = if (this.scale() > 0) this.stripTrailingZeros() else this
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }

    fun addDimensions(value: BigDecimal, change: BigDecimal): BigDecimal {
        require(value >= BigDecimal.ZERO) { "Value must be positive." }
        require(change >= BigDecimal.ZERO) { "Change must be positive." }
        return value.add(change).normalizeDecimal()
    }

    fun subtractDimensions(value: BigDecimal, change: BigDecimal): BigDecimal {
        require(value >= BigDecimal.ZERO) { "Value must be positive." }
        require(change >= BigDecimal.ZERO) { "Change must be positive." }
        val result = value.subtract(change)
        if (result < BigDecimal.ZERO) {
            throw IllegalArgumentException("Result cannot be negative.")
        }
        return result.normalizeDecimal()
    }

    fun halfDimension(value: BigDecimal): BigDecimal {
        require(value >= BigDecimal.ZERO) { "Value must be positive." }
        return value.divide(TWO, 10, RoundingMode.HALF_UP).normalizeDecimal()
    }

    fun doubleDimension(value: BigDecimal): BigDecimal {
        require(value >= BigDecimal.ZERO) { "Value must be positive." }
        return value.multiply(TWO).normalizeDecimal()
    }

    fun calculatePieceTotal(
        pieceSize: BigDecimal,
        quantity: Int,
        gap: BigDecimal = BigDecimal.ZERO
    ): BigDecimal {
        require(pieceSize > BigDecimal.ZERO) { "Piece size must be greater than zero." }
        require(quantity >= 1) { "Quantity must be at least 1." }
        require(gap >= BigDecimal.ZERO) { "Gap must be greater than or equal to zero." }

        if (quantity == 1) {
            return pieceSize.normalizeDecimal()
        }

        val totalPieces = pieceSize.multiply(BigDecimal(quantity))
        val totalGaps = gap.multiply(BigDecimal(quantity - 1))
        return totalPieces.add(totalGaps).normalizeDecimal()
    }

    fun calculateCenterPosition(
        available: BigDecimal,
        objectSize: BigDecimal
    ): CenterResult {
        require(available > BigDecimal.ZERO) { "Available width must be greater than zero." }
        require(objectSize > BigDecimal.ZERO) { "Object width must be greater than zero." }

        if (objectSize > available) {
            throw IllegalArgumentException("Object is larger than available space.")
        }

        val remaining = available.subtract(objectSize)
        val left = remaining.divide(TWO, 10, RoundingMode.HALF_UP).normalizeDecimal()
        val right = left

        val formula = "($available - $objectSize) ÷ 2 = $left"

        return CenterResult(
            left = left,
            right = right,
            available = available,
            objectSize = objectSize,
            remaining = remaining.normalizeDecimal(),
            formula = formula
        )
    }

    fun calculateArea(length: BigDecimal, width: BigDecimal): BigDecimal {
        require(length >= BigDecimal.ZERO && width >= BigDecimal.ZERO) {
            "Dimensions for area must not be negative."
        }
        return length.multiply(width).stripTrailingZeros()
    }

    fun calculateVolume(length: BigDecimal, width: BigDecimal, height: BigDecimal): BigDecimal {
        require(length >= BigDecimal.ZERO && width >= BigDecimal.ZERO && height >= BigDecimal.ZERO) {
            "Dimensions for volume must not be negative."
        }
        return length.multiply(width).multiply(height).stripTrailingZeros()
    }

    /**
     * Calculates surface area in square meters (m²) given dimensions in millimeters (mm).
     */
    fun calculateAreaInSquareMeters(lengthMm: BigDecimal, widthMm: BigDecimal): BigDecimal {
        val areaMm2 = calculateArea(lengthMm, widthMm)
        return areaMm2.divide(MM2_TO_M2, 10, RoundingMode.HALF_UP).stripTrailingZeros()
    }

    /**
     * Calculates volume in cubic meters (m³) given dimensions in millimeters (mm).
     */
    fun calculateVolumeInCubicMeters(
        lengthMm: BigDecimal,
        widthMm: BigDecimal,
        heightMm: BigDecimal
    ): BigDecimal {
        val volumeMm3 = calculateVolume(lengthMm, widthMm, heightMm)
        return volumeMm3.divide(MM3_TO_M3, 10, RoundingMode.HALF_UP).stripTrailingZeros()
    }
}
