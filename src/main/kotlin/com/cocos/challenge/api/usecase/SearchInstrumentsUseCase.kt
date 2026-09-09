package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.InstrumentResponse
import com.cocos.challenge.application.data.InstrumentFilter

interface SearchInstrumentsUseCase {
    fun execute(filter: InstrumentFilter): List<InstrumentResponse>
}
