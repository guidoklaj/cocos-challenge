package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderAggregate

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Int): Order?

    /**
     * Returns the user's order history pre-aggregated by `(instrumentId, side, status)`.
     * Callers combine these rows in Kotlin (`BalanceCalculator`, `PositionBuilder`) to
     * derive cash and share balances without loading raw orders into memory.
     */
    fun aggregateByUser(userId: Int): List<OrderAggregate>

    fun lockUserForOrderWrite(userId: Int)
}
