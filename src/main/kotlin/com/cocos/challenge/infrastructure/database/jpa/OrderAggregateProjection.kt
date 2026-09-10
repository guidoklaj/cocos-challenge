package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.domain.model.OrderAggregate
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import java.math.BigDecimal

/**
 * Constructor-expression target for the aggregate JPQL query. `SUM(size)` returns
 * `Long` in JPQL so we keep it as `Long` here and narrow to `Int` in `toDomain()`.
 */
class OrderAggregateProjection(
    val instrumentId: Int,
    val side: OrderSide,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val totalSize: Long
) {
    fun toDomain(): OrderAggregate = OrderAggregate(
        instrumentId = instrumentId,
        side = side,
        status = status,
        totalAmount = totalAmount,
        totalSize = totalSize.toInt()
    )
}
