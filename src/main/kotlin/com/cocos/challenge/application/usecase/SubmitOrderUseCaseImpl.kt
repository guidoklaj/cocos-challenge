package com.cocos.challenge.application.usecase

import com.cocos.challenge.api.request.SubmitOrderRequest
import com.cocos.challenge.api.response.OrderResponse
import com.cocos.challenge.api.usecase.SubmitOrderUseCase
import com.cocos.challenge.application.exception.InstrumentNotFoundException
import com.cocos.challenge.application.exception.MarketDataNotFoundException
import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.repository.InstrumentRepository
import com.cocos.challenge.application.repository.MarketDataRepository
import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.domain.exception.InvalidOrderException
import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SubmitOrderUseCaseImpl(
    private val userRepository: UserRepository,
    private val instrumentRepository: InstrumentRepository,
    private val orderRepository: OrderRepository,
    private val marketDataRepository: MarketDataRepository,
    @Value("\${cocos.cash-instrument-ticker:ARS}")
    private val cashTicker: String
) : SubmitOrderUseCase {

    @Transactional
    override fun execute(request: SubmitOrderRequest): OrderResponse {
        val userId = request.userId
        val side = request.side
        val type = request.type

        userRepository.findById(userId) ?: throw UserNotFoundException(userId)

        val instrument = resolveInstrument(request, side)
        val price = resolvePrice(side, type, instrument, request.price)
        val size = resolveSize(request.size, request.amount, price)
        val existingOrders = orderRepository.findByUserId(userId)

        val order = Order.create(
            userId = userId,
            instrumentId = instrument.id,
            side = side,
            size = size,
            price = price,
            type = type,
            existingOrders = existingOrders
        )
        return OrderResponse.from(orderRepository.save(order))
    }

    private fun resolveInstrument(request: SubmitOrderRequest, side: OrderSide): Instrument {
        if (side.isCashMovement) {
            return instrumentRepository.findByTicker(cashTicker)
                ?: throw InstrumentNotFoundException(cashTicker)
        }
        val instrument = when {
            request.instrumentId != null -> instrumentRepository.findById(request.instrumentId)
            !request.ticker.isNullOrBlank() -> instrumentRepository.findByTicker(request.ticker.trim())
            else -> null
        } ?: throw InstrumentNotFoundException(request.ticker ?: request.instrumentId.toString())

        if (instrument.isCash()) {
            throw InvalidOrderException("Cannot BUY/SELL cash instruments; use CASH_IN or CASH_OUT")
        }
        return instrument
    }

    private fun resolvePrice(
        side: OrderSide,
        type: OrderType,
        instrument: Instrument,
        requestedPrice: BigDecimal?
    ): BigDecimal = when {
        side.isCashMovement -> BigDecimal.ONE
        type == OrderType.LIMIT -> requestedPrice!!
        else -> marketDataRepository.findLatestByInstrumentId(instrument.id)?.close
            ?: throw MarketDataNotFoundException(instrument.id)
    }

    private fun resolveSize(size: Int?, amount: BigDecimal?, price: BigDecimal): Int {
        if (size != null) return size
        val shares = amount!!.divide(price, 0, RoundingMode.FLOOR).toInt()
        if (shares <= 0) {
            throw InvalidOrderException("amount is too small to purchase a single share at price $price")
        }
        return shares
    }
}
