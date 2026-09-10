package com.cocos.challenge.usecase

import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.repository.MarketDataRepository
import com.cocos.challenge.application.repository.UserHoldingRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.application.usecase.GetPortfolioUseCaseImpl
import com.cocos.challenge.domain.model.UserHolding
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetPortfolioUseCaseImplTest {

    private val userRepository = mock<UserRepository>()
    private val userHoldingRepository = mock<UserHoldingRepository>()
    private val marketDataRepository = mock<MarketDataRepository>()

    private val useCase = GetPortfolioUseCaseImpl(userRepository, userHoldingRepository, marketDataRepository)

    @Test
    fun `returns portfolio with positions and total account value`() {
        whenever(userRepository.findById(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserId(USER.id)).thenReturn(listOf(HOLDING))
        whenever(marketDataRepository.findLatestForInstrumentIds(listOf(STOCK.id)))
            .thenReturn(mapOf(STOCK.id to MARKET_DATA))

        val response = useCase.execute(USER.id)

        assertEquals(1, response.positions.size)
        assertEquals(STOCK.ticker, response.positions[0].ticker)
        assertEquals(HOLDING.heldShares, response.positions[0].quantity)
        assertEquals(
            USER.availableCash.add(MARKET_DATA.close.multiply(BigDecimal(HOLDING.heldShares))),
            response.totalAccountValue
        )
    }

    @Test
    fun `returns empty positions when no positionable holdings`() {
        val zeroHolding = HOLDING.copy(heldShares = 0)
        val cashHolding = UserHolding(id = 2, userId = USER.id, instrument = CASH_INSTRUMENT,
            availableShares = 1000, heldShares = 1000, totalBuyCost = BigDecimal("1000"))
        whenever(userRepository.findById(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserId(USER.id)).thenReturn(listOf(zeroHolding, cashHolding))

        val response = useCase.execute(USER.id)

        assertTrue(response.positions.isEmpty())
        assertEquals(USER.availableCash, response.totalAccountValue)
    }

    @Test
    fun `user not found throws UserNotFoundException`() {
        whenever(userRepository.findById(99)).thenReturn(null)

        assertFailsWith<UserNotFoundException> { useCase.execute(99) }
    }
}
