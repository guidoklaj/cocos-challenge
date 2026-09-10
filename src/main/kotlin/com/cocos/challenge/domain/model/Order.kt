package com.cocos.challenge.domain.model

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
         * Domain factory that decides the order's initial status against pre-computed
         * balance scalars supplied by the caller.
         *
         * - BUY / CASH_OUT require `availableCash`; the order is `REJECTED` if it doesn't
         *   cover `size * price`.
         * - SELL requires `availableShares` for the target instrument; `REJECTED` if it
         *   doesn't cover `size`.
         * - CASH_IN always funds.
         * - Once funded, MARKET orders and cash movements are `FILLED`; LIMIT orders stay `NEW`.
         *
         * Callers are expected to serialize concurrent creations for the same user (e.g. via
         * a per-user lock) so the scalars they compute from the DB reflect committed state.
         */
        fun create(
            userId: Int,
            instrumentId: Int,
            side: OrderSide,
            size: Int,
            price: BigDecimal,
            type: OrderType,
            availableCash: BigDecimal? = null,
            availableShares: Int? = null,
            now: LocalDateTime = LocalDateTime.now()
        ): Order {
            val totalAmount = price.multiply(BigDecimal(size))
            val funded = when (side) {
                OrderSide.BUY, OrderSide.CASH_OUT ->
                    requireNotNull(availableCash) {
                        "availableCash is required for $side orders"
                    } >= totalAmount
                OrderSide.SELL ->
                    requireNotNull(availableShares) {
                        "availableShares is required for SELL orders"
                    } >= size
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
