package com.example.workbench.model

import java.math.BigDecimal

data class WorkshopDimension(
    val id: String,
    val name: String,
    val category: DimensionCategory,
    val lengthMm: BigDecimal? = null,
    val widthMm: BigDecimal? = null,
    val heightMm: BigDecimal? = null,
    val thicknessMm: BigDecimal? = null,
    val unit: DimensionUnit = DimensionUnit.CM,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val has3dDimensions: Boolean
        get() = lengthMm != null && widthMm != null && heightMm != null

    val has2dDimensions: Boolean
        get() = lengthMm != null && widthMm != null
}
