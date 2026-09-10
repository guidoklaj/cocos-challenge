package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.infrastructure.database.entity.UserHoldingEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserHoldingJpaRepository : JpaRepository<UserHoldingEntity, Int> {
    fun findByUserId(userId: Int): List<UserHoldingEntity>
    fun findByUserIdAndInstrumentId(userId: Int, instrumentId: Int): UserHoldingEntity?
}
