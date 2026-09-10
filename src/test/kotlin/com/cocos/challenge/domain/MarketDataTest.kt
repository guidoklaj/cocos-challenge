package com.cocos.challenge.domain

import com.cocos.challenge.domain.model.MarketData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class MarketDataTest {

    private fun marketData(close: String, previousClose: String) = MarketData(
        id = 1, instrumentId = 1,
        high = BigDecimal("110"), low = BigDecimal("90"), open = BigDecimal("100"),
        close = BigDecimal(close), previousClose = BigDecimal(previousClose),
        date = LocalDate.now()
    )

    @Test
    fun `positive return when close is above previous close`() {
        // (110 - 100) / 100 * 100 = 10.00%
        assertEquals(BigDecimal("10.00"), marketData("110", "100").dailyReturnPercentage)
    }

    @Test
    fun `negative return when close is below previous close`() {
        // (90 - 100) / 100 * 100 = -10.00%
        assertEquals(BigDecimal("-10.00"), marketData("90", "100").dailyReturnPercentage)
    }

    @Test
    fun `zero return when close equals previous close`() {
        assertEquals(BigDecimal("0.00"), marketData("100", "100").dailyReturnPercentage)
    }

    @Test
    fun `zero return when previous close is zero to avoid division by zero`() {
        assertEquals(BigDecimal.ZERO, marketData("100", "0").dailyReturnPercentage)
    }

    @Test
    fun `fractional return is rounded to two decimal places`() {
        // (101 - 99) / 99 * 100 = 2.020202... → 2.02
        assertEquals(BigDecimal("2.02"), marketData("101", "99").dailyReturnPercentage)
    }
}
