package com.cocos.challenge.usecase

import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.InstrumentType
import com.cocos.challenge.domain.model.MarketData
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.OrderType
import com.cocos.challenge.domain.model.User
import com.cocos.challenge.domain.model.UserHolding
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

val STOCK = Instrument(id = 1, ticker = "GGAL", name = "Grupo Financiero Galicia", type = InstrumentType.STOCK)
val CASH_INSTRUMENT = Instrument(id = 2, ticker = "ARS", name = "Pesos Argentinos", type = InstrumentType.CURRENCY)

val USER = User(id = 1, email = "test@test.com", accountNumber = "00000001", availableCash = BigDecimal("10000.00"))

val MARKET_DATA = MarketData(
    id = 1, instrumentId = 1,
    high = BigDecimal("110"), low = BigDecimal("90"),
    open = BigDecimal("100"), close = BigDecimal("100.00"),
    previousClose = BigDecimal("95.00"),
    date = LocalDate.now()
)

val HOLDING = UserHolding(
    id = 1, userId = 1, instrument = STOCK,
    availableShares = 10, heldShares = 10,
    totalBuyCost = BigDecimal("800.00")
)

fun order(
    id: Int? = 1,
    side: OrderSide = OrderSide.BUY,
    status: OrderStatus = OrderStatus.NEW,
    size: Int = 5,
    price: BigDecimal = BigDecimal("100.00"),
    type: OrderType = OrderType.LIMIT
) = Order(
    id = id, userId = USER.id, instrumentId = STOCK.id,
    side = side, size = size, price = price,
    type = type, status = status,
    datetime = LocalDateTime.now()
)
