package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderAggregate
import com.cocos.challenge.infrastructure.database.entity.OrderEntity
import com.cocos.challenge.infrastructure.database.jpa.OrderJpaRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import javax.sql.DataSource
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderRepositoryImpl(
    private val jpa: OrderJpaRepository,
    @PersistenceContext private val em: EntityManager,
    dataSource: DataSource
) : OrderRepository {

    /**
     * Detected once at construction time so the lock method doesn't pay a metadata cost
     * per call. Postgres → advisory lock; anything else falls back to row-level FOR UPDATE.
     */
    private val supportsAdvisoryLock: Boolean = runCatching {
        dataSource.connection.use {
            it.metaData.databaseProductName.contains("PostgreSQL", ignoreCase = true)
        }
    }.getOrDefault(false)

    override fun save(order: Order): Order {
        val entity = order.id?.let { id ->
            jpa.findById(id).getOrNull()?.apply {
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

    override fun aggregateByUser(userId: Int): List<OrderAggregate> =
        jpa.aggregateByUser(userId).map { it.toDomain() }

    override fun lockUserForOrderWrite(userId: Int) {
        if (supportsAdvisoryLock) {
            em.createNativeQuery("SELECT pg_advisory_xact_lock(:id)")
                .setParameter("id", userId.toLong())
                .singleResult
        } else {
            em.createNativeQuery("SELECT id FROM users WHERE id = :id FOR UPDATE")
                .setParameter("id", userId)
                .singleResult
        }
    }
}
