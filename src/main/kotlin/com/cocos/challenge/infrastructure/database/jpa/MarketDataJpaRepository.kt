package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.infrastructure.database.entity.MarketDataEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MarketDataJpaRepository : JpaRepository<MarketDataEntity, Int> {

    fun findFirstByInstrumentIdOrderByDateDesc(instrumentId: Int): MarketDataEntity?

    @Query(
        """
        SELECT m FROM MarketDataEntity m
        WHERE m.instrumentId IN :ids
          AND m.date = (
              SELECT MAX(m2.date) FROM MarketDataEntity m2
              WHERE m2.instrumentId = m.instrumentId
          )
        """
    )
    fun findLatestForInstruments(@Param("ids") ids: Collection<Int>): List<MarketDataEntity>
}
