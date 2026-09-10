package com.cocos.challenge.usecase

import com.cocos.challenge.application.exception.OrderNotCancellableException
import com.cocos.challenge.application.exception.OrderNotFoundException
import com.cocos.challenge.application.exception.UserNotFoundException
import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.application.repository.UserHoldingRepository
import com.cocos.challenge.application.repository.UserRepository
import com.cocos.challenge.application.usecase.CancelOrderUseCaseImpl
import com.cocos.challenge.domain.model.Order
import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderStatus
import com.cocos.challenge.domain.model.User
import com.cocos.challenge.domain.model.UserHolding
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CancelOrderUseCaseImplTest {

    private val orderRepository = mock<OrderRepository>()
    private val userRepository = mock<UserRepository>()
    private val userHoldingRepository = mock<UserHoldingRepository>()

    private val useCase = CancelOrderUseCaseImpl(orderRepository, userRepository, userHoldingRepository)

    @Test
    fun `cancelling a NEW buy order restores cash and marks order CANCELLED`() {
        val newBuy = order(side = OrderSide.BUY, status = OrderStatus.NEW, size = 5)
        whenever(orderRepository.findById(1)).thenReturn(newBuy)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as User }
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }

        val response = useCase.execute(1)

        assertEquals(OrderStatus.CANCELLED.name, response.status)
        verify(userRepository).save(any())
        verify(userHoldingRepository, never()).save(any())
    }

    @Test
    fun `cancelling a NEW sell order restores available shares`() {
        val newSell = order(side = OrderSide.SELL, status = OrderStatus.NEW, size = 3)
        whenever(orderRepository.findById(1)).thenReturn(newSell)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(USER)
        whenever(userHoldingRepository.findByUserAndInstrument(USER.id, STOCK.id)).thenReturn(HOLDING)
        whenever(userHoldingRepository.save(any())).thenAnswer { inv -> inv.arguments[0] as UserHolding }
        whenever(orderRepository.save(any())).thenAnswer { inv -> (inv.arguments[0] as Order).copy(id = 1) }

        val response = useCase.execute(1)

        assertEquals(OrderStatus.CANCELLED.name, response.status)
        verify(userHoldingRepository).save(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `order not found throws OrderNotFoundException`() {
        whenever(orderRepository.findById(99)).thenReturn(null)

        assertFailsWith<OrderNotFoundException> { useCase.execute(99) }
    }

    @Test
    fun `cancelling a FILLED order throws OrderNotCancellableException`() {
        val filledOrder = order(status = OrderStatus.FILLED)
        whenever(orderRepository.findById(1)).thenReturn(filledOrder)

        assertFailsWith<OrderNotCancellableException> { useCase.execute(1) }
    }

    @Test
    fun `user not found throws UserNotFoundException`() {
        val newBuy = order(side = OrderSide.BUY, status = OrderStatus.NEW)
        whenever(orderRepository.findById(1)).thenReturn(newBuy)
        whenever(userRepository.findByIdForUpdate(USER.id)).thenReturn(null)

        assertFailsWith<UserNotFoundException> { useCase.execute(1) }
    }
}
