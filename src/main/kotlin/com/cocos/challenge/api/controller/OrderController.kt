package com.cocos.challenge.api.controller

import com.cocos.challenge.api.annotation.Idempotent
import com.cocos.challenge.api.request.SubmitOrderRequest
import com.cocos.challenge.api.response.OrderResponse
import com.cocos.challenge.api.usecase.CancelOrderUseCase
import com.cocos.challenge.api.usecase.SubmitOrderUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val submitOrderUseCase: SubmitOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase
) {
    @Idempotent
    @PostMapping
    fun submit(@Valid @RequestBody request: SubmitOrderRequest): ResponseEntity<OrderResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(submitOrderUseCase.execute(request))

    @PostMapping("/{orderId}/cancel")
    fun cancel(@PathVariable orderId: Int): OrderResponse =
        cancelOrderUseCase.execute(orderId)
}
