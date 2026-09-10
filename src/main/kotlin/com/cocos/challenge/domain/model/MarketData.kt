package com.cocos.challenge.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
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
) {
    val dailyReturnPercentage: BigDecimal
        get() = if (previousClose.signum() == 0) BigDecimal.ZERO
                else close.subtract(previousClose)
                    .divide(previousClose, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
}
