package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.Instrument
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "instruments")
class InstrumentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val ticker: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: InstrumentTypeEntity
) {
    fun toDomain(): Instrument = Instrument(
        id = requireNotNull(id) { "Persisted instrument must have an id" },
        ticker = ticker,
        name = name,
        type = type.domain
    )
}
