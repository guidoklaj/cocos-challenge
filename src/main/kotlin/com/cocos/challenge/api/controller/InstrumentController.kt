package com.cocos.challenge.api.controller

import com.cocos.challenge.api.response.InstrumentResponse
import com.cocos.challenge.api.usecase.SearchInstrumentsUseCase
import com.cocos.challenge.application.data.InstrumentFilter
import com.cocos.challenge.domain.model.InstrumentType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/instruments")
class InstrumentController(
    private val searchInstrumentsUseCase: SearchInstrumentsUseCase
) {
    @GetMapping
    fun search(
        @RequestParam(required = false) ticker: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) type: InstrumentType?
    ): List<InstrumentResponse> =
        searchInstrumentsUseCase.execute(InstrumentFilter(ticker = ticker, name = name, type = type))
}
