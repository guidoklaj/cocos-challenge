package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.UserHoldingRepository
import com.cocos.challenge.domain.model.UserHolding
import com.cocos.challenge.infrastructure.database.entity.UserHoldingEntity
import com.cocos.challenge.infrastructure.database.jpa.InstrumentJpaRepository
import com.cocos.challenge.infrastructure.database.jpa.UserHoldingJpaRepository
import org.springframework.stereotype.Repository

@Repository
class UserHoldingRepositoryImpl(
    private val jpa: UserHoldingJpaRepository,
    private val instrumentJpa: InstrumentJpaRepository
) : UserHoldingRepository {

    override fun findByUserId(userId: Int): List<UserHolding> =
        jpa.findByUserId(userId).map { it.toDomain() }

    override fun findByUserAndInstrument(userId: Int, instrumentId: Int): UserHolding? =
        jpa.findByUserIdAndInstrumentId(userId, instrumentId)?.toDomain()

    override fun save(holding: UserHolding): UserHolding {
        val entity = holding.id?.let { id ->
            jpa.findById(id).orElse(null)?.apply {
                availableShares = holding.availableShares
                heldShares = holding.heldShares
                totalBuyCost = holding.totalBuyCost
            }
        } ?: UserHoldingEntity(
            userId = holding.userId,
            instrument = instrumentJpa.getReferenceById(holding.instrumentId),
            availableShares = holding.availableShares,
            heldShares = holding.heldShares,
            totalBuyCost = holding.totalBuyCost
        )
        return jpa.save(entity).toDomain()
    }
}
