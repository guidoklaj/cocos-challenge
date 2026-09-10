package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.UserHolding

interface UserHoldingRepository {
    fun findByUserId(userId: Int): List<UserHolding>
    fun findByUserAndInstrument(userId: Int, instrumentId: Int): UserHolding?
    fun save(holding: UserHolding): UserHolding
}
