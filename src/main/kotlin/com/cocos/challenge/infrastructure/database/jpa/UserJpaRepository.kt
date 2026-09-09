package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.infrastructure.database.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Int>
