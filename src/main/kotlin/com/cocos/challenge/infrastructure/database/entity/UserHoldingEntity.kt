package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.UserHolding
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instrument_id", nullable = false)
    val instrument: InstrumentEntity,

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
        instrument = instrument.toDomain(),
        availableShares = availableShares,
        heldShares = heldShares,
        totalBuyCost = totalBuyCost
    )
}
