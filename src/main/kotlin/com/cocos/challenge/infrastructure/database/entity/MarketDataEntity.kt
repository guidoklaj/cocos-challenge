package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.MarketData
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "marketdata")
class MarketDataEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "instrument_id", nullable = false)
    val instrumentId: Int,

    @Column(nullable = false)
    val high: BigDecimal,

    @Column(nullable = false)
    val low: BigDecimal,

    @Column(nullable = false)
    val open: BigDecimal,

    @Column(nullable = false)
    val close: BigDecimal,

    @Column(name = "previous_close", nullable = false)
    val previousClose: BigDecimal,

    @Column(nullable = false)
    val date: LocalDate
) {
    fun toDomain(): MarketData = MarketData(
        id = requireNotNull(id),
        instrumentId = instrumentId,
        high = high,
        low = low,
        open = open,
        close = close,
        previousClose = previousClose,
        date = date
    )
}
