package com.cocos.challenge.domain

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.InstrumentType
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.OrderType
import com.cocos.challenge.domain.model.UserHolding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class UserHoldingTest {

    private val stock = Instrument(id = 1, ticker = "GGAL", name = "Galicia", type = InstrumentType.STOCK)
    private val cash = Instrument(id = 2, ticker = "ARS", name = "Pesos", type = InstrumentType.CURRENCY)

    private val holding = UserHolding(
        id = 1, userId = 1, instrument = stock,
        availableShares = 10, heldShares = 10, totalBuyCost = BigDecimal("800.00")
    )

    private fun order(side: OrderSide, size: Int, price: BigDecimal, status: OrderStatus = OrderStatus.FILLED) = Order(
        id = 1, userId = 1, instrumentId = stock.id, side = side, size = size, price = price,
        type = OrderType.MARKET, status = status, datetime = LocalDateTime.now()
    )

    @Test
    fun `averageBuyPrice is totalBuyCost divided by heldShares`() {
        assertEquals(BigDecimal("80.000000"), holding.averageBuyPrice)
    }

    @Test
    fun `averageBuyPrice is zero when heldShares is zero`() {
        assertEquals(BigDecimal.ZERO, UserHolding.empty(1, stock).averageBuyPrice)
    }

    @Test
    fun `hasEnoughShares is true when availableShares covers the size`() {
        assertTrue(holding.hasEnoughShares(10))
    }

    @Test
    fun `hasEnoughShares is false when size exceeds availableShares`() {
        assertFalse(holding.hasEnoughShares(11))
    }

    @Nested
    inner class IsPositionable {
        @Test
        fun `true for stock holding with held shares`() {
            assertTrue(holding.isPositionable())
        }

        @Test
        fun `false when heldShares is zero`() {
            assertFalse(UserHolding.empty(1, stock).isPositionable())
        }

        @Test
        fun `false for cash instrument regardless of shares`() {
            val cashHolding = UserHolding(id = 1, userId = 1, instrument = cash,
                availableShares = 100, heldShares = 100, totalBuyCost = BigDecimal("100"))
            assertFalse(cashHolding.isPositionable())
        }
    }

    @Nested
    inner class ApplyOrderBuy {
        @Test
        fun `FILLED buy increases shares and cost`() {
            val result = holding.applyOrder(order(OrderSide.BUY, 5, BigDecimal("100"), OrderStatus.FILLED))
            assertNotNull(result)
            assertEquals(15, result!!.heldShares)
            assertEquals(15, result.availableShares)
            assertEquals(BigDecimal("1300.00"), result.totalBuyCost)
        }

        @Test
        fun `NEW limit buy returns null (holding unchanged, cash reserved in user)`() {
            assertNull(holding.applyOrder(order(OrderSide.BUY, 5, BigDecimal("100"), OrderStatus.NEW)))
        }

        @Test
        fun `REJECTED buy returns null`() {
            assertNull(holding.applyOrder(order(OrderSide.BUY, 5, BigDecimal("100"), OrderStatus.REJECTED)))
        }
    }

    @Nested
    inner class ApplyOrderSell {
        @Test
        fun `FILLED sell decreases shares and proportional cost`() {
            val result = holding.applyOrder(order(OrderSide.SELL, 5, BigDecimal("100"), OrderStatus.FILLED))
            assertNotNull(result)
            assertEquals(5, result!!.heldShares)
            assertEquals(5, result.availableShares)
            // cost reduced by avgBuyPrice(80) * 5 = 400 → 800 - 400 = 400
            assertEquals(0, BigDecimal("400").compareTo(result.totalBuyCost))
        }

        @Test
        fun `NEW limit sell reserves shares without changing heldShares`() {
            val result = holding.applyOrder(order(OrderSide.SELL, 3, BigDecimal("100"), OrderStatus.NEW))
            assertNotNull(result)
            assertEquals(10, result!!.heldShares)
            assertEquals(7, result.availableShares)
        }
    }

    @Test
    fun `cancelOrder restores availableShares`() {
        val sellOrder = order(OrderSide.SELL, 3, BigDecimal("100"), OrderStatus.NEW)
        val afterReserve = holding.applyOrder(sellOrder)!!
        val afterCancel = afterReserve.cancelOrder(sellOrder)
        assertEquals(holding.availableShares, afterCancel.availableShares)
    }

    @Test
    fun `CASH_IN and CASH_OUT orders return null`() {
        assertNull(holding.applyOrder(order(OrderSide.CASH_IN, 100, BigDecimal("1"))))
        assertNull(holding.applyOrder(order(OrderSide.CASH_OUT, 100, BigDecimal("1"))))
    }

    @Test
    fun `empty creates a zero holding for the given user and instrument`() {
        val empty = UserHolding.empty(userId = 5, instrument = stock)
        assertEquals(0, empty.availableShares)
        assertEquals(0, empty.heldShares)
        assertEquals(BigDecimal.ZERO, empty.totalBuyCost)
        assertEquals(5, empty.userId)
        assertEquals(stock, empty.instrument)
    }
}
