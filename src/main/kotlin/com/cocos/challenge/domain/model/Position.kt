package com.cocos.challenge.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Position(
    val holding: UserHolding,
    val marketData: MarketData?
) {
    val instrument: Instrument get() = holding.instrument
    val quantity: Int get() = holding.heldShares

    val marketValue: BigDecimal
        get() = (marketData?.close ?: BigDecimal.ZERO).multiply(BigDecimal(quantity))

    val dailyReturnPercentage: BigDecimal
        get() = marketData?.dailyReturnPercentage ?: BigDecimal.ZERO

    val totalReturnPercentage: BigDecimal
        get() {
            val avg = holding.averageBuyPrice
            return if (avg.signum() == 0) BigDecimal.ZERO
                   else (marketData?.close ?: BigDecimal.ZERO).subtract(avg)
                       .divide(avg, 6, RoundingMode.HALF_UP)
                       .multiply(BigDecimal(100))
                       .setScale(2, RoundingMode.HALF_UP)
        }

    companion object {
        fun from(holding: UserHolding, marketData: MarketData?): Position {
            require(holding.isPositionable()) { "Holding is not positionable" }
            return Position(holding = holding, marketData = marketData)
        }
    }
}
