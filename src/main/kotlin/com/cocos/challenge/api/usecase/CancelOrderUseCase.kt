package com.cocos.challenge.api.usecase

import com.cocos.challenge.api.response.OrderResponse

/** Cancels a NEW order and releases any reserved cash or shares back to the user. */
interface CancelOrderUseCase {
    fun execute(orderId: Int): OrderResponse
}
