package com.cocos.challenge.domain.model

import java.math.BigDecimal

data class User(
    val id: Int,
    val email: String,
    val accountNumber: String,
    val availableCash: BigDecimal
) {
    fun canAfford(amount: BigDecimal): Boolean = availableCash >= amount

    fun cancelOrder(order: Order): User = copy(availableCash = availableCash + order.totalAmount)

    fun applyOrder(order: Order): User {
        val delta = when (order.side) {
            OrderSide.BUY      -> -order.totalAmount
            OrderSide.SELL     -> if (order.status == OrderStatus.FILLED) order.totalAmount else BigDecimal.ZERO
            OrderSide.CASH_IN  -> order.totalAmount
            OrderSide.CASH_OUT -> -order.totalAmount
        }
        return copy(availableCash = availableCash + delta)
    }
}
