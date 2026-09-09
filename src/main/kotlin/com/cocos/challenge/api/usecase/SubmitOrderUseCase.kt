package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.request.SubmitOrderRequest
import com.cocos.challenge.api.response.OrderResponse

interface SubmitOrderUseCase {
    fun execute(request: SubmitOrderRequest): OrderResponse
}
