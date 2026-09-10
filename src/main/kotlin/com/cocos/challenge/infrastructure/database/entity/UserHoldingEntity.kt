package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.UserHolding
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "user_holdings")
class UserHoldingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(name = "instrument_id", nullable = false)
    val instrumentId: Int,

    @Column(name = "available_shares", nullable = false)
    var availableShares: Int,

    @Column(name = "held_shares", nullable = false)
    var heldShares: Int,

    @Column(name = "total_buy_cost", nullable = false)
    var totalBuyCost: BigDecimal
) {
    fun toDomain(): UserHolding = UserHolding(
        id = id,
        userId = userId,
        instrumentId = instrumentId,
        availableShares = availableShares,
        heldShares = heldShares,
        totalBuyCost = totalBuyCost
    )

    companion object {
        fun fromDomain(holding: UserHolding): UserHoldingEntity = UserHoldingEntity(
            id = holding.id,
            userId = holding.userId,
            instrumentId = holding.instrumentId,
            availableShares = holding.availableShares,
            heldShares = holding.heldShares,
            totalBuyCost = holding.totalBuyCost
        )
    }
}
