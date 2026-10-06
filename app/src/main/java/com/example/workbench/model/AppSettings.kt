package com.example.workbench.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String): AppThemeMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUnit: DimensionUnit = DimensionUnit.CM,
    val decimalPlaces: Int = 2,
    val showArea: Boolean = true,
    val showVolume: Boolean = true,
    val showDimensionDiagram: Boolean = true,
    val showCalculationFormula: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
)
