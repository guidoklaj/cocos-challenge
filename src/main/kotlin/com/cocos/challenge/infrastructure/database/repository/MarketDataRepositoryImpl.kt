package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.MarketDataRepository
import com.cocos.challenge.domain.model.MarketData
import com.cocos.challenge.infrastructure.database.jpa.MarketDataJpaRepository
import org.springframework.stereotype.Repository

@Repository
class MarketDataRepositoryImpl(
    private val jpa: MarketDataJpaRepository
) : MarketDataRepository {

    override fun findLatestByInstrumentId(instrumentId: Int): MarketData? =
        jpa.findFirstByInstrumentIdOrderByDateDesc(instrumentId)?.toDomain()

    override fun findLatestForInstrumentIds(instrumentIds: Collection<Int>): Map<Int, MarketData> {
        if (instrumentIds.isEmpty()) return emptyMap()
        return jpa.findLatestForInstruments(instrumentIds)
            .associateBy({ it.instrumentId }, { it.toDomain() })
    }
}
