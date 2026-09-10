package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.Order

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Int): Order?
}
