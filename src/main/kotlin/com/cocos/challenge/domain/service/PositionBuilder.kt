package com.cocos.challenge.domain.service

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.MarketData
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.Position
import java.math.BigDecimal

/**
 * Builds a `Position` for a given instrument from the user's order history and
 * the latest market data snapshot.
 *
 * Returns `null` when the instrument is not a tradable asset (cash) or when the
 * user has no net quantity — a portfolio doesn't show zero-share rows.
 *
 * Lives in the domain layer because "what a position is" and "when a position
 * exists" are domain concerns, not application orchestration.
 */
class PositionBuilder(
    private val instrument: Instrument,
    private val orders: List<Order>,
    private val marketData: MarketData?
) {
    fun build(): Position? {
        if (instrument.type.isCash) return null

        val quantity = BalanceCalculator.heldQuantity(orders, instrument.id)
        if (quantity == 0) return null

        return Position(
            instrument = instrument,
            quantity = quantity,
            currentPrice = marketData?.close ?: BigDecimal.ZERO,
            averageBuyPrice = BalanceCalculator.averageBuyPrice(orders, instrument.id)
        )
    }
}
