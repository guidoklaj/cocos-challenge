package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderStatus

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Int): Order?
    fun findByUserId(userId: Int): List<Order>
    fun findByUserIdAndStatuses(userId: Int, statuses: Set<OrderStatus>): List<Order>
}
