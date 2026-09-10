package com.cocos.challenge.application.usecase

import com.cocos.challenge.api.response.PortfolioResponse
import com.cocos.challenge.api.usecase.GetPortfolioUseCase
import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.repository.MarketDataRepository
import com.cocos.challenge.application.repository.UserHoldingRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.domain.model.Portfolio
import com.cocos.challenge.domain.model.Position
import com.cocos.challenge.domain.model.UserHolding
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPortfolioUseCaseImpl(
    private val userRepository: UserRepository,
    private val userHoldingRepository: UserHoldingRepository,
    private val marketDataRepository: MarketDataRepository
) : GetPortfolioUseCase {

    @Transactional(readOnly = true)
    override fun execute(userId: Int): PortfolioResponse {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
        val holdings = userHoldingRepository.findByUserId(userId)
            .filter { it.isPositionable() }

        return PortfolioResponse.from(
            Portfolio(
                user = user,
                availableCash = user.availableCash,
                positions = buildPositions(holdings)
            )
        )
    }

    private fun buildPositions(holdings: List<UserHolding>): List<Position> {
        if (holdings.isEmpty()) return emptyList()
        val marketDataByInstrument = marketDataRepository.findLatestForInstrumentIds(holdings.map { it.instrumentId })
        return holdings.map { holding ->
            Position.from(holding, marketDataByInstrument[holding.instrumentId])
        }
    }
}
