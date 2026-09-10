package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.request.SubmitOrderRequest
import com.cocos.challenge.api.response.OrderResponse

/**
 * Validates, prices and submits an order. Returns FILLED for market orders, NEW for limit orders,
 * or REJECTED if the user lacks sufficient funds or shares.
 */
interface SubmitOrderUseCase {
    fun execute(request: SubmitOrderRequest): OrderResponse
}
