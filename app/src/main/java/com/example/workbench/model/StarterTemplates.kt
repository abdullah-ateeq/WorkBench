package com.example.workbench.model

import java.math.BigDecimal

data class DimensionPreset(
    val templateName: String,
    val category: DimensionCategory,
    val defaultName: String,
    val length: BigDecimal? = null,
    val width: BigDecimal? = null,
    val height: BigDecimal? = null,
    val thickness: BigDecimal? = null,
    val unit: DimensionUnit = DimensionUnit.CM,
    val notes: String = "Starting template"
)

object StarterTemplates {
    val presets: List<DimensionPreset> = listOf(
        DimensionPreset(
            templateName = "Workbench",
            category = DimensionCategory.WORKBENCH,
            defaultName = "Main Workbench",
            length = BigDecimal("180"),
            width = BigDecimal("60"),
            height = BigDecimal("85"),
            thickness = BigDecimal("5"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Standard workbench reference"
        ),
        DimensionPreset(
            templateName = "Table",
            category = DimensionCategory.TABLE,
            defaultName = "Dining Table",
            length = BigDecimal("160"),
            width = BigDecimal("90"),
            height = BigDecimal("75"),
            thickness = BigDecimal("4"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Dining / work table reference"
        ),
        DimensionPreset(
            templateName = "Shelf",
            category = DimensionCategory.SHELF,
            defaultName = "Wall Shelf",
            length = BigDecimal("120"),
            width = BigDecimal("30"),
            height = null,
            thickness = BigDecimal("1.8"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Shelf dimensions"
        ),
        DimensionPreset(
            templateName = "Cabinet",
            category = DimensionCategory.CABINET,
            defaultName = "Base Cabinet",
            length = BigDecimal("80"),
            width = BigDecimal("40"),
            height = BigDecimal("90"),
            thickness = BigDecimal("1.8"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Workshop base cabinet"
        ),
        DimensionPreset(
            templateName = "Door",
            category = DimensionCategory.DOOR,
            defaultName = "Cabinet Door",
            length = BigDecimal("80"),
            width = null,
            height = BigDecimal("200"),
            thickness = BigDecimal("4"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Door sizing"
        ),
        DimensionPreset(
            templateName = "Drawer",
            category = DimensionCategory.DRAWER,
            defaultName = "Shallow Drawer",
            length = BigDecimal("50"),
            width = BigDecimal("40"),
            height = BigDecimal("15"),
            thickness = BigDecimal("1.5"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Drawer box"
        ),
        DimensionPreset(
            templateName = "Frame",
            category = DimensionCategory.FRAME,
            defaultName = "Picture / Face Frame",
            length = BigDecimal("60"),
            width = null,
            height = BigDecimal("90"),
            thickness = BigDecimal("2.5"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Frame dimensions"
        ),
        DimensionPreset(
            templateName = "Panel",
            category = DimensionCategory.PANEL,
            defaultName = "Side Panel",
            length = BigDecimal("100"),
            width = BigDecimal("50"),
            height = null,
            thickness = BigDecimal("1.2"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Plywood sheet / panel"
        ),
        DimensionPreset(
            templateName = "Board",
            category = DimensionCategory.BOARD,
            defaultName = "Lumber Board",
            length = BigDecimal("200"),
            width = BigDecimal("15"),
            height = null,
            thickness = BigDecimal("2.5"),
            unit = DimensionUnit.CM,
            notes = "Starting template: Stock lumber board"
        ),
        DimensionPreset(
            templateName = "Custom",
            category = DimensionCategory.CUSTOM,
            defaultName = "Custom Measurement",
            length = null,
            width = null,
            height = null,
            thickness = null,
            unit = DimensionUnit.CM,
            notes = "Starting template: Blank reference"
        )
    )
}
