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

    val isRejected: Boolean
        get() = status == OrderStatus.REJECTED

    val creditsCash: Boolean
        get() = side == OrderSide.CASH_IN || (side == OrderSide.SELL && status == OrderStatus.FILLED)

    fun cancelled(): Order = copy(status = OrderStatus.CANCELLED)

    companion object {
        fun create(
            user: User,
            instrumentId: Int,
            side: OrderSide,
            size: Int,
            price: BigDecimal,
            type: OrderType,
            holding: UserHolding?,
            now: LocalDateTime = LocalDateTime.now()
        ): Order {
            val totalAmount = price.multiply(BigDecimal(size))
            val funded = when (side) {
                OrderSide.BUY, OrderSide.CASH_OUT -> user.canAfford(totalAmount)
                OrderSide.SELL                     -> holding?.hasEnoughShares(size) ?: false
                OrderSide.CASH_IN                  -> true
            }
            val status = when {
                !funded                                          -> OrderStatus.REJECTED
                side.isCashMovement || type == OrderType.MARKET -> OrderStatus.FILLED
                else                                            -> OrderStatus.NEW
            }
            return Order(
                id = null,
                userId = user.id,
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
