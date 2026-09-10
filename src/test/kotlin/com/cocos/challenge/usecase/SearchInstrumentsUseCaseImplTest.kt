package com.cocos.challenge.usecase

import com.cocos.challenge.application.data.InstrumentFilter
import com.cocos.challenge.application.repository.InstrumentRepository
import com.cocos.challenge.application.usecase.SearchInstrumentsUseCaseImpl
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchInstrumentsUseCaseImplTest {

    private val instrumentRepository = mock<InstrumentRepository>()
    private val useCase = SearchInstrumentsUseCaseImpl(instrumentRepository)

    @Test
    fun `returns mapped instrument responses for matching filter`() {
        val filter = InstrumentFilter(ticker = "GG")
        whenever(instrumentRepository.findByFilter(filter)).thenReturn(listOf(STOCK))

        val result = useCase.execute(filter)

        assertEquals(1, result.size)
        assertEquals(STOCK.ticker, result[0].ticker)
        assertEquals(STOCK.type.displayName, result[0].type)
    }

    @Test
    fun `returns empty list when no instruments match`() {
        val filter = InstrumentFilter(ticker = "NOMATCH")
        whenever(instrumentRepository.findByFilter(filter)).thenReturn(emptyList())

        val result = useCase.execute(filter)

        assertTrue(result.isEmpty())
    }
}
