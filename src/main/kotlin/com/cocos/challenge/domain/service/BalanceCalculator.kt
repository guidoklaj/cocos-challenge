package com.cocos.challenge.domain.service

import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure domain logic to compute the cash and share balances from the order history.
 *
 * We follow real-broker semantics: NEW LIMIT orders reserve funds (BUY) or shares (SELL),
 * so they are not available for other orders until they are FILLED or CANCELLED/REJECTED.
 */
object BalanceCalculator {

    fun availableCash(orders: List<Order>): BigDecimal =
        orders.fold(BigDecimal.ZERO) { acc, order ->
            when {
                order.status == OrderStatus.FILLED -> acc.add(cashDelta(order))
                order.status == OrderStatus.NEW && order.side == OrderSide.BUY ->
                    acc.subtract(order.totalAmount)
                else -> acc
            }
        }

    /**
     * Shares available to be sold for a given instrument. FILLED buys/sells settle the
     * position; NEW sells reserve shares that can no longer be sold in another order.
     */
    fun availableShares(orders: List<Order>, instrumentId: Int): Int =
        orders.filter { it.instrumentId == instrumentId }.sumOf { order ->
            when {
                order.status == OrderStatus.FILLED && order.side == OrderSide.BUY -> order.size
                order.status == OrderStatus.FILLED && order.side == OrderSide.SELL -> -order.size
                order.status == OrderStatus.NEW && order.side == OrderSide.SELL -> -order.size
                else -> 0
            }
        }

    /**
     * Net quantity currently held (only FILLED orders). This is the accounting quantity
     * used for market-value calculations, independent of NEW SELL reservations.
     */
    fun heldQuantity(orders: List<Order>, instrumentId: Int): Int =
        orders.filter { it.instrumentId == instrumentId && it.status == OrderStatus.FILLED }
            .sumOf { order ->
                when (order.side) {
                    OrderSide.BUY -> order.size
                    OrderSide.SELL -> -order.size
                    else -> 0
                }
            }

    /**
     * Weighted average price of FILLED buys for a given instrument. Used to compute
     * total return against the current market price.
     */
    fun averageBuyPrice(orders: List<Order>, instrumentId: Int): BigDecimal {
        val buys = orders.filter {
            it.instrumentId == instrumentId &&
                it.status == OrderStatus.FILLED &&
                it.side == OrderSide.BUY
        }
        val totalSize = buys.sumOf { it.size }
        if (totalSize == 0) return BigDecimal.ZERO
        val totalCost = buys.fold(BigDecimal.ZERO) { acc, o -> acc.add(o.totalAmount) }
        return totalCost.divide(BigDecimal(totalSize), 6, RoundingMode.HALF_UP)
    }

    private fun cashDelta(order: Order): BigDecimal = when (order.side) {
        OrderSide.CASH_IN, OrderSide.SELL -> order.totalAmount
        OrderSide.CASH_OUT, OrderSide.BUY -> order.totalAmount.negate()
    }
}
