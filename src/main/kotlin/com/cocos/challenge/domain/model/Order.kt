package com.cocos.challenge.domain.model

import com.cocos.challenge.domain.service.BalanceCalculator
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val id: Int?,
    val instrumentId: Int,
    val userId: Int,
    val side: OrderSide,
    val size: Int,
    val price: BigDecimal,
    val type: OrderType,
    val status: OrderStatus,
    val datetime: LocalDateTime
) {
    val totalAmount: BigDecimal
        get() = price.multiply(BigDecimal(size))

    val isCancellable: Boolean
        get() = status == OrderStatus.NEW

    fun cancelled(): Order = copy(status = OrderStatus.CANCELLED)

    companion object {
        /**
         * Domain factory that decides the order's initial status from the user's order history.
         *
         * - BUY / CASH_OUT reject if the user has less available cash than `size * price`.
         * - SELL rejects if the user has less available shares of that instrument than `size`.
         * - CASH_IN always funds.
         * - Once funded, MARKET orders and cash movements are FILLED; LIMIT orders stay NEW.
         */
        fun create(
            userId: Int,
            instrumentId: Int,
            side: OrderSide,
            size: Int,
            price: BigDecimal,
            type: OrderType,
            existingOrders: List<Order>,
            now: LocalDateTime = LocalDateTime.now()
        ): Order {
            val totalAmount = price.multiply(BigDecimal(size))
            val funded = when (side) {
                OrderSide.BUY, OrderSide.CASH_OUT ->
                    BalanceCalculator.availableCash(existingOrders) >= totalAmount
                OrderSide.SELL ->
                    BalanceCalculator.availableShares(existingOrders, instrumentId) >= size
                OrderSide.CASH_IN -> true
            }
            val status = when {
                !funded -> OrderStatus.REJECTED
                side.isCashMovement || type == OrderType.MARKET -> OrderStatus.FILLED
                else -> OrderStatus.NEW
            }
            return Order(
                id = null,
                userId = userId,
                instrumentId = instrumentId,
                side = side,
                size = size,
                price = price,
                type = type,
                status = status,
                datetime = now
            )
        }
    }
}
