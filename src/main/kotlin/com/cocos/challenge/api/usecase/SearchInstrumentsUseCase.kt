package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.InstrumentResponse
import com.cocos.challenge.application.data.InstrumentFilter

/** Returns all instruments whose ticker or name contains the given search terms (case-insensitive substring match). */
interface SearchInstrumentsUseCase {
    fun execute(filter: InstrumentFilter): List<InstrumentResponse>
}
