package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.infrastructure.database.entity.OrderEntity
import com.cocos.challenge.infrastructure.database.jpa.OrderJpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderRepositoryImpl(
    private val jpa: OrderJpaRepository
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = order.id?.let { existingId ->
            jpa.findById(existingId).getOrNull()?.apply {
                instrumentId = order.instrumentId
                userId = order.userId
                side = order.side
                size = order.size
                price = order.price
                type = order.type
                status = order.status
                datetime = order.datetime
            } ?: OrderEntity.fromDomain(order)
        } ?: OrderEntity.fromDomain(order)
        return jpa.save(entity).toDomain()
    }

    override fun findById(id: Int): Order? =
        jpa.findById(id).getOrNull()?.toDomain()

    override fun findByUserId(userId: Int): List<Order> =
        jpa.findByUserId(userId).map { it.toDomain() }

    override fun findByUserIdAndStatuses(userId: Int, statuses: Set<OrderStatus>): List<Order> =
        jpa.findByUserIdAndStatusIn(userId, statuses).map { it.toDomain() }
}
