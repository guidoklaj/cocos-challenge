package com.cocos.challenge.domain.service

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.MarketData
import com.cocos.challenge.domain.model.Position
import com.cocos.challenge.domain.model.UserHolding
import java.math.BigDecimal

class PositionBuilder(
    private val instrument: Instrument,
    private val holding: UserHolding,
    private val marketData: MarketData?
) {
    fun build(): Position? {
        if (instrument.isCash()) return null
        if (holding.heldShares == 0) return null

        return Position(
            instrument = instrument,
            quantity = holding.heldShares,
            currentPrice = marketData?.close ?: BigDecimal.ZERO,
            averageBuyPrice = holding.averageBuyPrice
        )
    }
}
