package com.cocos.challenge.application.usecase

import com.cocos.challenge.api.response.OrderResponse
import com.cocos.challenge.api.usecase.CancelOrderUseCase
import com.cocos.challenge.application.exception.OrderNotFoundException
import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.domain.exception.OrderNotCancellableException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CancelOrderUseCaseImpl(
    private val orderRepository: OrderRepository
) : CancelOrderUseCase {

    @Transactional
    override fun execute(orderId: Int): OrderResponse {
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
        if (!order.isCancellable) {
            throw OrderNotCancellableException(orderId, order.status.name)
        }
        return OrderResponse.from(orderRepository.save(order.cancelled()))
    }
}
