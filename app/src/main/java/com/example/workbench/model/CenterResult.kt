package com.example.workbench.model

import java.math.BigDecimal

data class CenterResult(
    val left: BigDecimal,
    val right: BigDecimal,
    val available: BigDecimal,
    val objectSize: BigDecimal,
    val remaining: BigDecimal,
    val formula: String
)
