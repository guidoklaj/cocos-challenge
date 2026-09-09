package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.MarketData

interface MarketDataRepository {
    fun findLatestByInstrumentId(instrumentId: Int): MarketData?
    fun findLatestForInstrumentIds(instrumentIds: Collection<Int>): Map<Int, MarketData>
}
