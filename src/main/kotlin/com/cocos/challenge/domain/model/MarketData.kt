package com.cocos.challenge.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class MarketData(
    val id: Int,
    val instrumentId: Int,
    val high: BigDecimal,
    val low: BigDecimal,
    val open: BigDecimal,
    val close: BigDecimal,
    val previousClose: BigDecimal,
    val date: LocalDate
)
