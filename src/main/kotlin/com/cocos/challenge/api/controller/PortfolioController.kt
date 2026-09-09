package com.cocos.challenge.api.controller

import com.cocos.challenge.api.response.PortfolioResponse
import com.cocos.challenge.api.usecase.GetPortfolioUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users/{userId}")
class PortfolioController(
    private val getPortfolioUseCase: GetPortfolioUseCase
) {
    @GetMapping("/portfolio")
    fun portfolio(@PathVariable userId: Int): PortfolioResponse =
        getPortfolioUseCase.execute(userId)
}
