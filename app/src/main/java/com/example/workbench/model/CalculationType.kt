package com.example.workbench.model

enum class CalculationType(val displayName: String) {
    ADD("Add"),
    SUBTRACT("Subtract"),
    HALF("Half"),
    DOUBLE("Double"),
    TOTAL_FROM_PIECES("Total From Pieces"),
    CENTER_POSITION("Center Position");

    companion object {
        fun fromString(value: String): CalculationType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: ADD
        }
    }
}
