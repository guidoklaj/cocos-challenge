package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.PortfolioResponse

/** Returns total account value, available cash and open positions with market value and return % for a given user. */
interface GetPortfolioUseCase {
    fun execute(userId: Int): PortfolioResponse
}
