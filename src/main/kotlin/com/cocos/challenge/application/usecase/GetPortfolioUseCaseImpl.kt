package com.cocos.challenge.application.usecase

import com.cocos.challenge.api.response.PortfolioResponse
import com.cocos.challenge.api.usecase.GetPortfolioUseCase
import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.repository.InstrumentRepository
import com.cocos.challenge.application.repository.MarketDataRepository
import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.Portfolio
import com.cocos.challenge.domain.model.Position
import com.cocos.challenge.domain.service.BalanceCalculator
import com.cocos.challenge.domain.service.PositionBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPortfolioUseCaseImpl(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val instrumentRepository: InstrumentRepository,
    private val marketDataRepository: MarketDataRepository
) : GetPortfolioUseCase {

    @Transactional(readOnly = true)
    override fun execute(userId: Int): PortfolioResponse {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
        val orders = orderRepository.findByUserId(userId)

        return PortfolioResponse.from(
            Portfolio(
                user = user,
                availableCash = BalanceCalculator.availableCash(orders),
                positions = buildPositions(orders)
            )
        )
    }

    private fun buildPositions(orders: List<Order>): List<Position> {
        val instrumentIds = orders.map { it.instrumentId }.toSet()
        if (instrumentIds.isEmpty()) return emptyList()

        val instruments = instrumentIds.mapNotNull { instrumentRepository.findById(it) }
        val marketDataByInstrument = marketDataRepository.findLatestForInstrumentIds(
            instruments.map(Instrument::id)
        )

        return instruments.mapNotNull { instrument ->
            PositionBuilder(
                instrument = instrument,
                orders = orders,
                marketData = marketDataByInstrument[instrument.id]
            ).build()
        }
    }
}
