package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.infrastructure.database.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderEntity, Int> {
    fun findByUserId(userId: Int): List<OrderEntity>
    fun findByUserIdAndStatusIn(userId: Int, statuses: Collection<OrderStatus>): List<OrderEntity>
}
