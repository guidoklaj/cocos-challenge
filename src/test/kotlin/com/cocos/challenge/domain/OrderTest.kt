package com.cocos.challenge.domain

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.InstrumentType
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.OrderType
import com.cocos.challenge.domain.model.User
import com.cocos.challenge.domain.model.UserHolding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderTest {

    private val stock = Instrument(id = 1, ticker = "GGAL", name = "Galicia", type = InstrumentType.STOCK)
    private val richUser = User(id = 1, email = "a@b.com", accountNumber = "001", availableCash = BigDecimal("100000"))
    private val brokeUser = User(id = 2, email = "b@b.com", accountNumber = "002", availableCash = BigDecimal("1.00"))
    private val holding = UserHolding(
        id = 1, userId = 1, instrument = stock,
        availableShares = 10, heldShares = 10, totalBuyCost = BigDecimal("1000")
    )
    private val emptyHolding = UserHolding.empty(userId = 1, instrument = stock)

    @Nested
    inner class CreateBuy {
        @Test
        fun `market buy with enough cash is FILLED`() {
            val order = Order.create(richUser, stock.id, OrderSide.BUY, 5, BigDecimal("100"), OrderType.MARKET, null)
            assertEquals(OrderStatus.FILLED, order.status)
        }

        @Test
        fun `limit buy with enough cash is NEW`() {
            val order = Order.create(richUser, stock.id, OrderSide.BUY, 5, BigDecimal("100"), OrderType.LIMIT, null)
            assertEquals(OrderStatus.NEW, order.status)
        }

        @Test
        fun `buy with insufficient cash is REJECTED`() {
            val order = Order.create(brokeUser, stock.id, OrderSide.BUY, 5, BigDecimal("100"), OrderType.MARKET, null)
            assertEquals(OrderStatus.REJECTED, order.status)
        }
    }

    @Nested
    inner class CreateSell {
        @Test
        fun `market sell with enough shares is FILLED`() {
            val order = Order.create(richUser, stock.id, OrderSide.SELL, 5, BigDecimal("100"), OrderType.MARKET, holding)
            assertEquals(OrderStatus.FILLED, order.status)
        }

        @Test
        fun `limit sell with enough shares is NEW`() {
            val order = Order.create(richUser, stock.id, OrderSide.SELL, 5, BigDecimal("100"), OrderType.LIMIT, holding)
            assertEquals(OrderStatus.NEW, order.status)
        }

        @Test
        fun `sell with no holding is REJECTED`() {
            val order = Order.create(richUser, stock.id, OrderSide.SELL, 5, BigDecimal("100"), OrderType.MARKET, null)
            assertEquals(OrderStatus.REJECTED, order.status)
        }

        @Test
        fun `sell with insufficient shares is REJECTED`() {
            val order = Order.create(richUser, stock.id, OrderSide.SELL, 100, BigDecimal("100"), OrderType.MARKET, holding)
            assertEquals(OrderStatus.REJECTED, order.status)
        }

        @Test
        fun `sell with empty holding is REJECTED`() {
            val order = Order.create(richUser, stock.id, OrderSide.SELL, 1, BigDecimal("100"), OrderType.MARKET, emptyHolding)
            assertEquals(OrderStatus.REJECTED, order.status)
        }
    }

    @Nested
    inner class CreateCash {
        @Test
        fun `cash-in is always FILLED`() {
            val order = Order.create(brokeUser, stock.id, OrderSide.CASH_IN, 99999, BigDecimal("1"), OrderType.MARKET, null)
            assertEquals(OrderStatus.FILLED, order.status)
        }

        @Test
        fun `cash-out with enough cash is FILLED`() {
            val order = Order.create(richUser, stock.id, OrderSide.CASH_OUT, 100, BigDecimal("1"), OrderType.MARKET, null)
            assertEquals(OrderStatus.FILLED, order.status)
        }

        @Test
        fun `cash-out with insufficient cash is REJECTED`() {
            val order = Order.create(brokeUser, stock.id, OrderSide.CASH_OUT, 100, BigDecimal("1"), OrderType.MARKET, null)
            assertEquals(OrderStatus.REJECTED, order.status)
        }
    }

    @Nested
    inner class Properties {
        @Test
        fun `totalAmount is price times size`() {
            val order = Order.create(richUser, stock.id, OrderSide.BUY, 3, BigDecimal("50"), OrderType.MARKET, null)
            assertEquals(BigDecimal("150"), order.totalAmount)
        }

        @Test
        fun `isCancellable is true only for NEW orders`() {
            val new = Order.create(richUser, stock.id, OrderSide.BUY, 1, BigDecimal("10"), OrderType.LIMIT, null)
            val filled = Order.create(richUser, stock.id, OrderSide.BUY, 1, BigDecimal("10"), OrderType.MARKET, null)
            val rejected = Order.create(brokeUser, stock.id, OrderSide.BUY, 1, BigDecimal("10"), OrderType.MARKET, null)
            assertTrue(new.isCancellable)
            assertFalse(filled.isCancellable)
            assertFalse(rejected.isCancellable)
        }

        @Test
        fun `creditsCash is true for CASH_IN and FILLED SELL`() {
            val cashIn = Order.create(richUser, stock.id, OrderSide.CASH_IN, 1, BigDecimal("1"), OrderType.MARKET, null)
            val filledSell = Order.create(richUser, stock.id, OrderSide.SELL, 1, BigDecimal("100"), OrderType.MARKET, holding)
            val newSell = Order.create(richUser, stock.id, OrderSide.SELL, 1, BigDecimal("100"), OrderType.LIMIT, holding)
            val buy = Order.create(richUser, stock.id, OrderSide.BUY, 1, BigDecimal("100"), OrderType.MARKET, null)
            assertTrue(cashIn.creditsCash)
            assertTrue(filledSell.creditsCash)
            assertFalse(newSell.creditsCash)
            assertFalse(buy.creditsCash)
        }

        @Test
        fun `cancelled returns a copy with CANCELLED status`() {
            val order = Order.create(richUser, stock.id, OrderSide.BUY, 1, BigDecimal("10"), OrderType.LIMIT, null)
            val cancelled = order.cancelled()
            assertEquals(OrderStatus.CANCELLED, cancelled.status)
            assertEquals(order.size, cancelled.size)
        }
    }
}
