package com.cocos.challenge.domain.service

import com.cocos.challenge.domain.model.OrderAggregate
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure domain logic to compute cash / share balances from a pre-aggregated view of the
 * user's order history (one row per `(instrumentId, side, status)`).
 *
 * We follow real-broker semantics: NEW LIMIT orders reserve funds (BUY) or shares (SELL),
 * so they are not available for other orders until they are FILLED or CANCELLED/REJECTED.
 *
 * The functions are deliberately pure over `List<OrderAggregate>` — the SQL side just
 * has to return the sums; the sign rules stay expressed in one place, in Kotlin.
 */
object BalanceCalculator {

    fun availableCash(aggregates: List<OrderAggregate>): BigDecimal =
        aggregates.fold(BigDecimal.ZERO) { acc, agg -> acc.add(agg.cashDelta()) }

    fun availableShares(aggregates: List<OrderAggregate>, instrumentId: Int): Int =
        aggregates.filter { it.instrumentId == instrumentId }.sumOf { it.sharesDelta() }

    fun heldQuantity(aggregates: List<OrderAggregate>, instrumentId: Int): Int =
        aggregates
            .filter { it.instrumentId == instrumentId && it.status == OrderStatus.FILLED }
            .sumOf { agg ->
                when (agg.side) {
                    OrderSide.BUY -> agg.totalSize
                    OrderSide.SELL -> -agg.totalSize
                    else -> 0
                }
            }

    fun averageBuyPrice(aggregates: List<OrderAggregate>, instrumentId: Int): BigDecimal {
        val buys = aggregates.filter {
            it.instrumentId == instrumentId &&
                it.status == OrderStatus.FILLED &&
                it.side == OrderSide.BUY
        }
        val totalSize = buys.sumOf { it.totalSize }
        if (totalSize == 0) return BigDecimal.ZERO
        val totalCost = buys.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.totalAmount) }
        return totalCost.divide(BigDecimal(totalSize), 6, RoundingMode.HALF_UP)
    }

    private fun OrderAggregate.cashDelta(): BigDecimal = when {
        status == OrderStatus.FILLED && (side == OrderSide.CASH_IN || side == OrderSide.SELL) ->
            totalAmount
        status == OrderStatus.FILLED && (side == OrderSide.CASH_OUT || side == OrderSide.BUY) ->
            totalAmount.negate()
        status == OrderStatus.NEW && side == OrderSide.BUY ->
            totalAmount.negate()
        else -> BigDecimal.ZERO
    }

    private fun OrderAggregate.sharesDelta(): Int = when {
        status == OrderStatus.FILLED && side == OrderSide.BUY -> totalSize
        status == OrderStatus.FILLED && side == OrderSide.SELL -> -totalSize
        status == OrderStatus.NEW && side == OrderSide.SELL -> -totalSize
        else -> 0
    }
}
