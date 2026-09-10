package com.cocos.challenge.domain.model

import java.math.BigDecimal

/**
 * Pre-aggregated slice of a user's order history, grouped by (instrument, side, status).
 *
 * `BalanceCalculator` and `Order.create` work off a list of these instead of the raw
 * `Order` list so that the repository can compute the aggregation in SQL — the whole
 * order history never has to be loaded into memory to answer "how much cash / how many
 * shares does the user have available?".
 */
data class OrderAggregate(
    val instrumentId: Int,
    val side: OrderSide,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val totalSize: Int
)
