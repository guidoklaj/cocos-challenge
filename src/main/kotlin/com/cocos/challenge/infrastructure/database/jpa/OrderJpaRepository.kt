package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.infrastructure.database.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderJpaRepository : JpaRepository<OrderEntity, Int> {

    @Query(
        """
        SELECT new com.cocos.challenge.infrastructure.database.jpa.OrderAggregateProjection(
            o.instrumentId,
            o.side,
            o.status,
            SUM(o.size * o.price),
            SUM(o.size)
        )
        FROM OrderEntity o
        WHERE o.userId = :userId
        GROUP BY o.instrumentId, o.side, o.status
        """
    )
    fun aggregateByUser(@Param("userId") userId: Int): List<OrderAggregateProjection>
}
