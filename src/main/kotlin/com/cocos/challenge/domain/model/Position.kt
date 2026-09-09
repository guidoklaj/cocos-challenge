package com.cocos.challenge.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Position(
    val instrument: Instrument,
    val quantity: Int,
    val currentPrice: BigDecimal,
    val averageBuyPrice: BigDecimal
) {
    val marketValue: BigDecimal
        get() = currentPrice.multiply(BigDecimal(quantity))

    val totalReturnPercentage: BigDecimal
        get() = if (averageBuyPrice.signum() == 0) {
            BigDecimal.ZERO
        } else {
            currentPrice.subtract(averageBuyPrice)
                .divide(averageBuyPrice, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        }
}
