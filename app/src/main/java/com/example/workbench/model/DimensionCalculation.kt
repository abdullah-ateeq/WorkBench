package com.example.workbench.model

import java.math.BigDecimal

data class DimensionCalculation(
    val id: String,
    val name: String,
    val calculationType: CalculationType,
    val inputValues: List<BigDecimal>,
    val result: BigDecimal,
    val unit: String,
    val formula: String,
    val secondaryResult: BigDecimal? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
