package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.infrastructure.database.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderEntity, Int>
