package com.cocos.challenge.domain

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.InstrumentType
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.OrderType
import com.cocos.challenge.domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class UserTest {

    private val user = User(id = 1, email = "a@b.com", accountNumber = "001", availableCash = BigDecimal("1000.00"))

    private fun order(side: OrderSide, size: Int, price: BigDecimal, status: OrderStatus = OrderStatus.FILLED) = Order(
        id = 1, userId = 1, instrumentId = 10, side = side, size = size, price = price,
        type = OrderType.MARKET, status = status, datetime = LocalDateTime.now()
    )

    @Test
    fun `canAfford returns true when cash covers the amount exactly`() {
        assertTrue(user.canAfford(BigDecimal("1000.00")))
    }

    @Test
    fun `canAfford returns false when amount exceeds available cash`() {
        assertFalse(user.canAfford(BigDecimal("1000.01")))
    }

    @Test
    fun `applyOrder BUY debits total amount`() {
        val result = user.applyOrder(order(OrderSide.BUY, 3, BigDecimal("100")))
        assertEquals(BigDecimal("700.00"), result.availableCash)
    }

    @Test
    fun `applyOrder SELL FILLED credits total amount`() {
        val result = user.applyOrder(order(OrderSide.SELL, 2, BigDecimal("200"), OrderStatus.FILLED))
        assertEquals(BigDecimal("1400.00"), result.availableCash)
    }

    @Test
    fun `applyOrder SELL NEW does not change cash`() {
        val result = user.applyOrder(order(OrderSide.SELL, 5, BigDecimal("100"), OrderStatus.NEW))
        assertEquals(BigDecimal("1000.00"), result.availableCash)
    }

    @Test
    fun `applyOrder CASH_IN credits total amount`() {
        val result = user.applyOrder(order(OrderSide.CASH_IN, 500, BigDecimal("1")))
        assertEquals(BigDecimal("1500.00"), result.availableCash)
    }

    @Test
    fun `applyOrder CASH_OUT debits total amount`() {
        val result = user.applyOrder(order(OrderSide.CASH_OUT, 300, BigDecimal("1")))
        assertEquals(BigDecimal("700.00"), result.availableCash)
    }

    @Test
    fun `cancelOrder restores reserved cash from a limit buy`() {
        val limitBuy = order(OrderSide.BUY, 4, BigDecimal("50"), OrderStatus.NEW)
        val withReserved = user.applyOrder(limitBuy) // cash goes down by 200
        val restored = withReserved.cancelOrder(limitBuy)
        assertEquals(user.availableCash, restored.availableCash)
    }
}
