package com.cocos.challenge.api.response

import com.cocos.challenge.domain.model.Order
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderResponse(
    val id: Int,
    val userId: Int,
    val instrumentId: Int,
    val side: String,
    val type: String,
    val status: String,
    val size: Int,
    val price: BigDecimal,
    val datetime: LocalDateTime
) {
    companion object {
        fun from(order: Order): OrderResponse = OrderResponse(
            id = requireNotNull(order.id) { "Persisted order must have an id" },
            userId = order.userId,
            instrumentId = order.instrumentId,
            side = order.side.name,
            type = order.type.name,
            status = order.status.name,
            size = order.size,
            price = order.price,
            datetime = order.datetime
        )
    }
}
