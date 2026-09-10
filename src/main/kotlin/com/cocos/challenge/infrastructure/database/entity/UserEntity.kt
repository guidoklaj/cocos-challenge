package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "account_number", nullable = false, unique = true)
    val accountNumber: String,

    @Column(name = "available_cash", nullable = false)
    var availableCash: BigDecimal
) {
    fun toDomain(): User = User(
        id = requireNotNull(id) { "Persisted user must have an id" },
        email = email,
        accountNumber = accountNumber,
        availableCash = availableCash
    )
}
