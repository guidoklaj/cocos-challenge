package com.cocos.challenge.application.repository

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.application.data.InstrumentFilter

interface InstrumentRepository {
    fun findById(id: Int): Instrument?
    fun findByTicker(ticker: String): Instrument?
    fun findByFilter(filter: InstrumentFilter): List<Instrument>
}
