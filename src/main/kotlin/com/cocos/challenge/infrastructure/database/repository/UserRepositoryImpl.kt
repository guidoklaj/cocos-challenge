package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.domain.model.User
import com.cocos.challenge.infrastructure.database.jpa.UserJpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class UserRepositoryImpl(
    private val jpa: UserJpaRepository
) : UserRepository {
    override fun findById(id: Int): User? =
        jpa.findById(id).getOrNull()?.toDomain()

    override fun findByIdForUpdate(id: Int): User? =
        jpa.findByIdForUpdate(id).getOrNull()?.toDomain()
}
