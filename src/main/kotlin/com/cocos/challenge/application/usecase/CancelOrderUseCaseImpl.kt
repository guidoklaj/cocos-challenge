package com.cocos.challenge.application.usecase

import com.cocos.challenge.api.response.OrderResponse
import com.cocos.challenge.api.usecase.CancelOrderUseCase
import com.cocos.challenge.application.exception.OrderNotCancellableException
import com.cocos.challenge.application.exception.OrderNotFoundException
import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.application.repository.UserHoldingRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.domain.model.OrderSide
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CancelOrderUseCaseImpl(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val userHoldingRepository: UserHoldingRepository
) : CancelOrderUseCase {

    @Transactional
    override fun execute(orderId: Int): OrderResponse {
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
        if (!order.isCancellable) throw OrderNotCancellableException(orderId, order.status.name)

        val user = userRepository.findByIdForUpdate(order.userId) ?: throw UserNotFoundException(order.userId)

        when (order.side) {
            OrderSide.BUY ->
                userRepository.save(user.cancelOrder(order))
            OrderSide.SELL -> {
                val holding = userHoldingRepository.findByUserAndInstrument(order.userId, order.instrumentId)
                    ?: error("Holding not found for SELL order $orderId")
                userHoldingRepository.save(holding.cancelOrder(order))
            }
            else -> {}
        }

        return OrderResponse.from(orderRepository.save(order.cancelled()))
    }
}
