package com.example.workbench.model

enum class DimensionCategory(val displayName: String) {
    WORKBENCH("Workbench"),
    TABLE("Table"),
    SHELF("Shelf"),
    CABINET("Cabinet"),
    DOOR("Door"),
    DRAWER("Drawer"),
    FRAME("Frame"),
    PANEL("Panel"),
    BOARD("Board"),
    CUSTOM("Custom");

    companion object {
        fun fromString(value: String): DimensionCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: CUSTOM
        }
    }
}
