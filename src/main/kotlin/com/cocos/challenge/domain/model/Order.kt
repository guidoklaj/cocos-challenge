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
        fun create(
            userId: Int,
            instrumentId: Int,
            side: OrderSide,
            size: Int,
            price: BigDecimal,
            type: OrderType,
            funded: Boolean,
            now: LocalDateTime = LocalDateTime.now()
        ): Order {
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
