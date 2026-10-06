package com.example.workbench

import com.example.workbench.data.WorkBenchJsonAdapter
import com.example.workbench.model.CalculationType
import com.example.workbench.model.DimensionCalculation
import com.example.workbench.model.DimensionCategory
import com.example.workbench.model.DimensionUnit
import com.example.workbench.model.WorkshopDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class WorkBenchSerializationTest {

    // 19. Dimension serialization
    @Test
    fun testDimensionSerializationRoundTrip() {
        val original = WorkshopDimension(
            id = "dim-123",
            name = "Main Workbench",
            category = DimensionCategory.WORKBENCH,
            lengthMm = BigDecimal("1800"),
            widthMm = BigDecimal("600"),
            heightMm = BigDecimal("850"),
            thicknessMm = BigDecimal("50"),
            unit = DimensionUnit.CM,
            notes = "Reference size for workshop",
            isFavorite = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val json = WorkBenchJsonAdapter.dimensionToJson(original)
        val deserialized = WorkBenchJsonAdapter.dimensionFromJson(json)

        assertEquals(original.id, deserialized.id)
        assertEquals(original.name, deserialized.name)
        assertEquals(original.category, deserialized.category)
        assertEquals(original.lengthMm, deserialized.lengthMm)
        assertEquals(original.widthMm, deserialized.widthMm)
        assertEquals(original.heightMm, deserialized.heightMm)
        assertEquals(original.thicknessMm, deserialized.thicknessMm)
        assertEquals(original.unit, deserialized.unit)
        assertEquals(original.notes, deserialized.notes)
        assertEquals(original.isFavorite, deserialized.isFavorite)
        assertEquals(original.createdAt, deserialized.createdAt)
        assertEquals(original.updatedAt, deserialized.updatedAt)
    }

    @Test
    fun testDimensionsListSerialization() {
        val list = listOf(
            WorkshopDimension(
                id = "1",
                name = "Shelf",
                category = DimensionCategory.SHELF,
                lengthMm = BigDecimal("1200"),
                widthMm = BigDecimal("300")
            ),
            WorkshopDimension(
                id = "2",
                name = "Table",
                category = DimensionCategory.TABLE,
                lengthMm = BigDecimal("1600"),
                widthMm = BigDecimal("900"),
                heightMm = BigDecimal("750")
            )
        )

        val jsonString = WorkBenchJsonAdapter.dimensionsToJson(list)
        val result = WorkBenchJsonAdapter.dimensionsFromJson(jsonString)

        assertEquals(2, result.size)
        assertEquals("Shelf", result[0].name)
        assertEquals("Table", result[1].name)
    }

    // 20. History serialization
    @Test
    fun testCalculationSerializationRoundTrip() {
        val original = DimensionCalculation(
            id = "calc-456",
            name = "Total Shelf Run",
            calculationType = CalculationType.TOTAL_FROM_PIECES,
            inputValues = listOf(BigDecimal("30"), BigDecimal("4"), BigDecimal("2")),
            result = BigDecimal("126"),
            unit = "cm",
            formula = "(30 × 4) + (2 × 3) = 126 cm",
            secondaryResult = null,
            notes = "4 slats",
            createdAt = 5000L
        )

        val json = WorkBenchJsonAdapter.calculationToJson(original)
        val deserialized = WorkBenchJsonAdapter.calculationFromJson(json)

        assertEquals(original.id, deserialized.id)
        assertEquals(original.name, deserialized.name)
        assertEquals(original.calculationType, deserialized.calculationType)
        assertEquals(original.inputValues, deserialized.inputValues)
        assertEquals(original.result, deserialized.result)
        assertEquals(original.unit, deserialized.unit)
        assertEquals(original.formula, deserialized.formula)
        assertEquals(original.notes, deserialized.notes)
        assertEquals(original.createdAt, deserialized.createdAt)
    }
}
