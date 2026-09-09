package com.cocos.challenge.application.usecase

import com.cocos.challenge.api.response.InstrumentResponse
import com.cocos.challenge.api.usecase.SearchInstrumentsUseCase
import com.cocos.challenge.application.repository.InstrumentRepository
import com.cocos.challenge.application.data.InstrumentFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchInstrumentsUseCaseImpl(
    private val instrumentRepository: InstrumentRepository
) : SearchInstrumentsUseCase {

    @Transactional(readOnly = true)
    override fun execute(filter: InstrumentFilter): List<InstrumentResponse> =
        instrumentRepository.findByFilter(filter).map(InstrumentResponse::from)
}
