package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.OrderType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "instrument_id", nullable = false)
    var instrumentId: Int,

    @Column(name = "user_id", nullable = false)
    var userId: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var side: OrderSide,

    @Column(nullable = false)
    var size: Int,

    @Column(nullable = false)
    var price: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: OrderType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    @Column(nullable = false)
    var datetime: LocalDateTime
) {
    fun toDomain(): Order = Order(
        id = id,
        instrumentId = instrumentId,
        userId = userId,
        side = side,
        size = size,
        price = price,
        type = type,
        status = status,
        datetime = datetime
    )

    companion object {
        fun fromDomain(order: Order): OrderEntity = OrderEntity(
            id = order.id,
            instrumentId = order.instrumentId,
            userId = order.userId,
            side = order.side,
            size = order.size,
            price = order.price,
            type = order.type,
            status = order.status,
            datetime = order.datetime
        )
    }
}
