package com.cocos.challenge.domain.model

import java.math.BigDecimal

data class Portfolio(
    val user: User,
    val availableCash: BigDecimal,
    val positions: List<Position>
) {
    val totalAccountValue: BigDecimal
        get() = positions.fold(availableCash) { acc, p -> acc.add(p.marketValue) }
}
