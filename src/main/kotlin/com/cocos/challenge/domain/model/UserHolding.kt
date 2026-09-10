package com.cocos.challenge.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class UserHolding(
    val id: Int?,
    val userId: Int,
    val instrumentId: Int,
    val availableShares: Int,
    val heldShares: Int,
    val totalBuyCost: BigDecimal
) {
    val averageBuyPrice: BigDecimal
        get() = if (heldShares == 0) BigDecimal.ZERO
                else totalBuyCost.divide(BigDecimal(heldShares), 6, RoundingMode.HALF_UP)

    fun hasEnoughShares(size: Int): Boolean = availableShares >= size

    fun cancelOrder(order: Order): UserHolding = copy(availableShares = availableShares + order.size)

    fun applyOrder(order: Order): UserHolding? = when (order.side) {
        OrderSide.BUY -> when (order.status) {
            OrderStatus.FILLED -> copy(
                availableShares = availableShares + order.size,
                heldShares      = heldShares + order.size,
                totalBuyCost    = totalBuyCost + order.totalAmount
            )
            else -> null // NEW: cash reserved in user, holding unchanged until filled
        }
        OrderSide.SELL -> when (order.status) {
            OrderStatus.FILLED -> copy(
                availableShares = availableShares - order.size,
                heldShares      = heldShares - order.size,
                totalBuyCost    = (totalBuyCost - averageBuyPrice.multiply(BigDecimal(order.size))).max(BigDecimal.ZERO)
            )
            else -> copy(availableShares = availableShares - order.size) // NEW: reserve shares
        }
        else -> null
    }

    companion object {
        fun empty(userId: Int, instrumentId: Int): UserHolding = UserHolding(
            id = null, userId = userId, instrumentId = instrumentId,
            availableShares = 0, heldShares = 0, totalBuyCost = BigDecimal.ZERO
        )
    }
}
