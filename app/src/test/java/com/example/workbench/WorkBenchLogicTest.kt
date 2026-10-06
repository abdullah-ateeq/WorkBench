package com.example.workbench

import com.example.workbench.model.AppSettings
import com.example.workbench.model.AppThemeMode
import com.example.workbench.model.DimensionCategory
import com.example.workbench.model.DimensionUnit
import com.example.workbench.model.WorkshopDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class WorkBenchLogicTest {

    // 21. Favorites sorting: Favorites first, then recently modified
    @Test
    fun testFavoriteSorting() {
        val item1 = WorkshopDimension(
            id = "1",
            name = "Regular item old",
            category = DimensionCategory.CUSTOM,
            isFavorite = false,
            updatedAt = 1000L
        )
        val item2 = WorkshopDimension(
            id = "2",
            name = "Regular item new",
            category = DimensionCategory.CUSTOM,
            isFavorite = false,
            updatedAt = 3000L
        )
        val item3 = WorkshopDimension(
            id = "3",
            name = "Favorite item old",
            category = DimensionCategory.CUSTOM,
            isFavorite = true,
            updatedAt = 2000L
        )

        val list = listOf(item1, item2, item3)
        val sorted = list.sortedWith(
            compareByDescending<WorkshopDimension> { it.isFavorite }
                .thenByDescending { it.updatedAt }
        )

        assertEquals("Favorite item old", sorted[0].name)
        assertEquals("Regular item new", sorted[1].name)
        assertEquals("Regular item old", sorted[2].name)
    }

    // 22. Settings persistence defaults
    @Test
    fun testSettingsDefaults() {
        val settings = AppSettings()
        assertEquals(AppThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.dynamicColor)
        assertEquals(DimensionUnit.CM, settings.defaultUnit)
        assertEquals(2, settings.decimalPlaces)
        assertTrue(settings.showArea)
        assertTrue(settings.showVolume)
        assertTrue(settings.showDimensionDiagram)
        assertTrue(settings.showCalculationFormula)
        assertTrue(settings.hapticFeedback)
        assertTrue(settings.confirmDelete)
    }

    // 23. Search case-insensitive filtering
    @Test
    fun testSearchFiltering() {
        val list = listOf(
            WorkshopDimension(
                id = "1",
                name = "Main Workbench",
                category = DimensionCategory.WORKBENCH,
                notes = "Solid beech"
            ),
            WorkshopDimension(
                id = "2",
                name = "Garage Shelf",
                category = DimensionCategory.SHELF,
                notes = "Use 18 mm plywood"
            ),
            WorkshopDimension(
                id = "3",
                name = "Kitchen Island",
                category = DimensionCategory.TABLE,
                notes = "Large top"
            )
        )

        // Search by name
        val nameQuery = "workbench"
        val nameMatches = list.filter {
            it.name.lowercase().contains(nameQuery) ||
                it.category.displayName.lowercase().contains(nameQuery) ||
                (it.notes?.lowercase()?.contains(nameQuery) == true)
        }
        assertEquals(1, nameMatches.size)
        assertEquals("Main Workbench", nameMatches[0].name)

        // Search by category
        val catQuery = "shelf"
        val catMatches = list.filter {
            it.name.lowercase().contains(catQuery) ||
                it.category.displayName.lowercase().contains(catQuery) ||
                (it.notes?.lowercase()?.contains(catQuery) == true)
        }
        assertEquals(1, catMatches.size)
        assertEquals("Garage Shelf", catMatches[0].name)

        // Search by notes
        val notesQuery = "plywood"
        val notesMatches = list.filter {
            it.name.lowercase().contains(notesQuery) ||
                it.category.displayName.lowercase().contains(notesQuery) ||
                (it.notes?.lowercase()?.contains(notesQuery) == true)
        }
        assertEquals(1, notesMatches.size)
        assertEquals("Garage Shelf", notesMatches[0].name)
    }

    // 24. Duplicate records
    @Test
    fun testDuplicateRecord() {
        val original = WorkshopDimension(
            id = "original-123",
            name = "Garage Cabinet",
            category = DimensionCategory.CABINET,
            lengthMm = BigDecimal("800"),
            widthMm = BigDecimal("400"),
            heightMm = BigDecimal("900"),
            thicknessMm = BigDecimal("18"),
            unit = DimensionUnit.CM,
            notes = "Prototype dimension",
            isFavorite = true,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val now = 2000L
        val duplicated = original.copy(
            id = UUID.randomUUID().toString(),
            name = "Copy of ${original.name}",
            createdAt = now,
            updatedAt = now
        )

        assertNotEquals(original.id, duplicated.id)
        assertEquals("Copy of Garage Cabinet", duplicated.name)
        assertEquals(original.category, duplicated.category)
        assertEquals(original.lengthMm, duplicated.lengthMm)
        assertEquals(original.widthMm, duplicated.widthMm)
        assertEquals(original.heightMm, duplicated.heightMm)
        assertEquals(original.thicknessMm, duplicated.thicknessMm)
        assertEquals(original.unit, duplicated.unit)
        assertEquals(original.notes, duplicated.notes)
    }
}
