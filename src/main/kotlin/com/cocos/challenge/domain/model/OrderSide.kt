package com.cocos.challenge.domain.model

enum class OrderSide(
    val isCashMovement: Boolean,
    val increasesCash: Boolean
) {
    BUY(isCashMovement = false, increasesCash = false),
    SELL(isCashMovement = false, increasesCash = true),
    CASH_IN(isCashMovement = true, increasesCash = true),
    CASH_OUT(isCashMovement = true, increasesCash = false)
}
