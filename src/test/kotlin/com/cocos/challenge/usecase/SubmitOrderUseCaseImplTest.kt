package com.cocos.challenge.usecase

import com.cocos.challenge.api.request.SubmitOrderRequest
import com.cocos.challenge.application.exception.InstrumentNotFoundException
import com.cocos.challenge.application.exception.MarketDataNotFoundException
import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.exception.InvalidOrderException
import com.cocos.challenge.application.repository.InstrumentRepository
import com.cocos.challenge.application.repository.MarketDataRepository
import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.application.repository.UserHoldingRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.application.usecase.SubmitOrderUseCaseImpl
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.OrderType
import com.cocos.challenge.domain.model.User
import com.cocos.challenge.domain.model.UserHolding
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SubmitOrderUseCaseImplTest {

    private val userRepository = mock<UserRepository>()
    private val userHoldingRepository = mock<UserHoldingRepository>()
    private val instrumentRepository = mock<InstrumentRepository>()
    private val orderRepository = mock<OrderRepository>()
    private val marketDataRepository = mock<MarketDataRepository>()

    private val useCase = SubmitOrderUseCaseImpl(
        userRepository, userHoldingRepository, instrumentRepository,
        orderRepository, marketDataRepository, cashTicker = "ARS"
    )

    @Test
    fun `market buy fills immediately at latest close`() {
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(null)
        whenever(marketDataRepository.findLatestByInstrumentId(STOCK.id)).thenReturn(MARKET_DATA)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }
        whenever(userHoldingRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as UserHolding }

        val response = useCase.execute(marketBuy(size = 5))

        assertEquals(OrderStatus.FILLED.name, response.status)
        assertEquals(MARKET_DATA.close, response.price)
        verify(userRepository).save(any())
        verify(userHoldingRepository).save(any())
    }

    @Test
    fun `limit buy stays NEW and reserves cash`() {
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(null)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }

        val response = useCase.execute(limitBuy(size = 5, price = BigDecimal("90")))

        assertEquals(OrderStatus.NEW.name, response.status)
        verify(userRepository).save(any())
        verify(userHoldingRepository, never()).save(any())
    }

    @Test
    fun `buy with insufficient cash is rejected`() {
        val brokeUser = USER.copy(availableCash = BigDecimal("10.00"))
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(brokeUser)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(null)
        whenever(marketDataRepository.findLatestByInstrumentId(STOCK.id)).thenReturn(MARKET_DATA)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }

        val response = useCase.execute(marketBuy(size = 5))

        assertEquals(OrderStatus.REJECTED.name, response.status)
        verify(userRepository, never()).save(any())
        verify(userHoldingRepository, never()).save(any())
    }

    @Test
    fun `market sell with enough shares fills and credits cash`() {
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(HOLDING)
        whenever(marketDataRepository.findLatestByInstrumentId(STOCK.id)).thenReturn(MARKET_DATA)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }
        whenever(userHoldingRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as UserHolding }

        val response = useCase.execute(marketSell(size = 5))

        assertEquals(OrderStatus.FILLED.name, response.status)
        verify(userRepository).save(any())
        verify(userHoldingRepository).save(any())
    }

    @Test
    fun `sell with insufficient shares is rejected`() {
        val thinHolding = HOLDING.copy(availableShares = 2)
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(thinHolding)
        whenever(marketDataRepository.findLatestByInstrumentId(STOCK.id)).thenReturn(MARKET_DATA)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }

        val response = useCase.execute(marketSell(size = 5))

        assertEquals(OrderStatus.REJECTED.name, response.status)
        verify(userRepository, never()).save(any())
        verify(userHoldingRepository, never()).save(any())
    }

    @Test
    fun `cash in fills at price one and credits user`() {
        whenever(instrumentRepository.findByTicker("ARS")).thenReturn(CASH_INSTRUMENT)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }

        val response = useCase.execute(cashIn(size = 5000))

        assertEquals(OrderStatus.FILLED.name, response.status)
        assertEquals(BigDecimal.ONE, response.price)
    }

    @Test
    fun `cash out fills at price one and debits user`() {
        whenever(instrumentRepository.findByTicker("ARS")).thenReturn(CASH_INSTRUMENT)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }

        val response = useCase.execute(cashOut(size = 1000))

        assertEquals(OrderStatus.FILLED.name, response.status)
        assertEquals(BigDecimal.ONE, response.price)
    }

    @Test
    fun `unknown ticker throws InstrumentNotFoundException`() {
        whenever(instrumentRepository.findByTicker("UNKNOWN")).thenReturn(null)

        assertFailsWith<InstrumentNotFoundException> {
            useCase.execute(marketBuy(ticker = "UNKNOWN", size = 1))
        }
    }

    @Test
    fun `unknown user throws UserNotFoundException`() {
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(null)

        assertFailsWith<UserNotFoundException> {
            useCase.execute(limitBuy(size = 1, price = BigDecimal("100")))
        }
    }

    @Test
    fun `missing market data throws MarketDataNotFoundException`() {
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(null)
        whenever(marketDataRepository.findLatestByInstrumentId(STOCK.id)).thenReturn(null)

        assertFailsWith<MarketDataNotFoundException> {
            useCase.execute(marketBuy(size = 1))
        }
    }

    @Test
    fun `buying a cash instrument throws InvalidOrderException`() {
        whenever(instrumentRepository.findByTicker("ARS")).thenReturn(CASH_INSTRUMENT)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)

        assertFailsWith<InvalidOrderException> {
            useCase.execute(
                SubmitOrderRequest(
                    userId = USER.id, ticker = "ARS",
                    side = OrderSide.BUY, type = OrderType.MARKET,
                    size = 1, amount = null, price = null, instrumentId = null
                )
            )
        }
    }

    @Test
    fun `amount is converted to whole shares via floor division`() {
        whenever(instrumentRepository.findByTicker("GGAL")).thenReturn(STOCK)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(null)
        whenever(marketDataRepository.findLatestByInstrumentId(STOCK.id)).thenReturn(MARKET_DATA) // close = 100
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }
        whenever(userHoldingRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as UserHolding }

        // 350 / 100 = 3.5 → 3 shares
        val response = useCase.execute(
            SubmitOrderRequest(
                userId = USER.id, ticker = "GGAL",
                side = OrderSide.BUY, type = OrderType.MARKET,
                size = null, amount = BigDecimal("350"), price = null, instrumentId = null
            )
        )

        assertEquals(3, response.size)
    }

    private fun marketBuy(ticker: String = "GGAL", size: Int) = SubmitOrderRequest(
        userId = USER.id, ticker = ticker, instrumentId = null,
        side = OrderSide.BUY, type = OrderType.MARKET,
        size = size, amount = null, price = null
    )

    private fun marketSell(size: Int) = SubmitOrderRequest(
        userId = USER.id, ticker = "GGAL", instrumentId = null,
        side = OrderSide.SELL, type = OrderType.MARKET,
        size = size, amount = null, price = null
    )

    private fun limitBuy(size: Int, price: BigDecimal) = SubmitOrderRequest(
        userId = USER.id, ticker = "GGAL", instrumentId = null,
        side = OrderSide.BUY, type = OrderType.LIMIT,
        size = size, amount = null, price = price
    )

    private fun cashIn(size: Int) = SubmitOrderRequest(
        userId = USER.id, ticker = null, instrumentId = null,
        side = OrderSide.CASH_IN, type = OrderType.MARKET,
        size = size, amount = null, price = null
    )

    private fun cashOut(size: Int) = SubmitOrderRequest(
        userId = USER.id, ticker = null, instrumentId = null,
        side = OrderSide.CASH_OUT, type = OrderType.MARKET,
        size = size, amount = null, price = null
    )
}
