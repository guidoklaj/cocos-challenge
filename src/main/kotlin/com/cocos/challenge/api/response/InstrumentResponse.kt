package com.cocos.challenge.api.response

import com.cocos.challenge.domain.model.Instrument

data class InstrumentResponse(
    val id: Int,
    val ticker: String,
    val name: String,
    val type: String
) {
    companion object {
        fun from(instrument: Instrument): InstrumentResponse = InstrumentResponse(
            id = instrument.id,
            ticker = instrument.ticker,
            name = instrument.name,
            type = instrument.type.displayName
        )
    }
}
