package com.cocos.challenge.api.response

import com.cocos.challenge.domain.model.Portfolio
import java.math.BigDecimal

data class PortfolioResponse(
    val userId: Int,
    val accountNumber: String,
    val totalAccountValue: BigDecimal,
    val availableCash: BigDecimal,
    val positions: List<PositionResponse>
) {
    companion object {
        fun from(portfolio: Portfolio): PortfolioResponse = PortfolioResponse(
            userId = portfolio.user.id,
            accountNumber = portfolio.user.accountNumber,
            totalAccountValue = portfolio.totalAccountValue,
            availableCash = portfolio.availableCash,
            positions = portfolio.positions.map(PositionResponse::from)
        )
    }
}

data class PositionResponse(
    val instrumentId: Int,
    val ticker: String,
    val name: String,
    val quantity: Int,
    val marketValue: BigDecimal,
    val totalReturnPercentage: BigDecimal
) {
    companion object {
        fun from(position: com.cocos.challenge.domain.model.Position): PositionResponse = PositionResponse(
            instrumentId = position.instrument.id,
            ticker = position.instrument.ticker,
            name = position.instrument.name,
            quantity = position.quantity,
            marketValue = position.marketValue,
            totalReturnPercentage = position.totalReturnPercentage
        )
    }
}
