package com.cocos.challenge.domain

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.InstrumentType
import com.cocos.challenge.domain.model.MarketData
import com.cocos.challenge.domain.model.Position
import com.cocos.challenge.domain.model.UserHolding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class PositionTest {

    private val stock = Instrument(id = 1, ticker = "GGAL", name = "Galicia", type = InstrumentType.STOCK)
    private val cash = Instrument(id = 2, ticker = "ARS", name = "Pesos", type = InstrumentType.CURRENCY)

    private val holding = UserHolding(
        id = 1, userId = 1, instrument = stock,
        availableShares = 10, heldShares = 10, totalBuyCost = BigDecimal("800.00")
    )

    private fun marketData(close: String, previousClose: String) = MarketData(
        id = 1, instrumentId = stock.id,
        high = BigDecimal("110"), low = BigDecimal("90"), open = BigDecimal("100"),
        close = BigDecimal(close), previousClose = BigDecimal(previousClose),
        date = LocalDate.now()
    )

    @Test
    fun `from throws when holding is not positionable`() {
        val empty = UserHolding.empty(userId = 1, instrument = stock)
        assertThrows<IllegalArgumentException> { Position.from(empty, null) }
    }

    @Test
    fun `from throws for cash instrument`() {
        val cashHolding = UserHolding(id = 1, userId = 1, instrument = cash,
            availableShares = 100, heldShares = 100, totalBuyCost = BigDecimal("100"))
        assertThrows<IllegalArgumentException> { Position.from(cashHolding, null) }
    }

    @Test
    fun `marketValue is close times quantity`() {
        val position = Position.from(holding, marketData("120", "100"))
        // 120 * 10 = 1200
        assertEquals(BigDecimal("1200"), position.marketValue)
    }

    @Test
    fun `marketValue is zero when market data is absent`() {
        val position = Position.from(holding, null)
        assertEquals(BigDecimal.ZERO, position.marketValue)
    }

    @Test
    fun `dailyReturnPercentage delegates to market data`() {
        // (120 - 100) / 100 * 100 = 20.00%
        val position = Position.from(holding, marketData("120", "100"))
        assertEquals(BigDecimal("20.00"), position.dailyReturnPercentage)
    }

    @Test
    fun `dailyReturnPercentage is zero when market data is absent`() {
        assertEquals(BigDecimal.ZERO, Position.from(holding, null).dailyReturnPercentage)
    }

    @Test
    fun `totalReturnPercentage compares close to average buy price`() {
        // avgBuyPrice = 800 / 10 = 80; close = 120 → (120 - 80) / 80 * 100 = 50.00%
        val position = Position.from(holding, marketData("120", "100"))
        assertEquals(BigDecimal("50.00"), position.totalReturnPercentage)
    }

    @Test
    fun `totalReturnPercentage is negative when current price is below average cost`() {
        // avgBuyPrice = 80; close = 60 → (60 - 80) / 80 * 100 = -25.00%
        val position = Position.from(holding, marketData("60", "100"))
        assertEquals(BigDecimal("-25.00"), position.totalReturnPercentage)
    }

    @Test
    fun `totalReturnPercentage is zero when average buy price is zero`() {
        val zeroAvgHolding = UserHolding(
            id = 1, userId = 1, instrument = stock,
            availableShares = 5, heldShares = 5, totalBuyCost = BigDecimal.ZERO
        )
        assertEquals(BigDecimal.ZERO, Position.from(zeroAvgHolding, marketData("100", "90")).totalReturnPercentage)
    }

    @Test
    fun `totalReturnPercentage treats absent market data as zero current price`() {
        // avgBuyPrice = 80; no close → (0 - 80) / 80 * 100 = -100.00%
        assertEquals(BigDecimal("-100.00"), Position.from(holding, null).totalReturnPercentage)
    }
}
