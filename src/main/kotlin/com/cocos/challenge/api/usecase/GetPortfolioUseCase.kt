package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.PortfolioResponse

interface GetPortfolioUseCase {
    fun execute(userId: Int): PortfolioResponse
}
