package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.User

interface UserRepository {
    fun findById(id: Int): User?
}
