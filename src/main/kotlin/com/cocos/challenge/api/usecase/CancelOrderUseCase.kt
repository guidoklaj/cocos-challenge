package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.OrderResponse

interface CancelOrderUseCase {
    fun execute(orderId: Int): OrderResponse
}
