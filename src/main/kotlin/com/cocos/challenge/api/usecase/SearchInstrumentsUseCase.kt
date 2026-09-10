package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.InstrumentResponse
import com.cocos.challenge.application.data.InstrumentFilter

/** Returns all instruments matching the given filter. Ticker and name are case-insensitive substring matches; type is exact. */
interface SearchInstrumentsUseCase {
    fun execute(filter: InstrumentFilter): List<InstrumentResponse>
}
