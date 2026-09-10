package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.infrastructure.database.entity.OrderEntity
import com.cocos.challenge.infrastructure.database.jpa.OrderJpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderRepositoryImpl(
    private val jpa: OrderJpaRepository
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = order.id?.let { id ->
            jpa.findById(id).getOrNull()?.apply {
                status = order.status
            } ?: OrderEntity.fromDomain(order)
        } ?: OrderEntity.fromDomain(order)
        return jpa.save(entity).toDomain()
    }

    override fun findById(id: Int): Order? =
        jpa.findById(id).getOrNull()?.toDomain()
}
