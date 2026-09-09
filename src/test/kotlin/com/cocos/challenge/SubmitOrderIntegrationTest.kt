package com.cocos.challenge

import com.cocos.challenge.application.repository.OrderRepository
import com.cocos.challenge.domain.model.OrderStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
class SubmitOrderIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper
    @Autowired lateinit var orderRepository: OrderRepository

    @Test
    fun `market buy order for a stock fills immediately at the latest close`() {
        val payload = mapOf(
            "userId" to 1,
            "ticker" to "PAMP",
            "side" to "BUY",
            "type" to "MARKET",
            "size" to 10
        )

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("FILLED"))
            .andExpect(jsonPath("$.side").value("BUY"))
            .andExpect(jsonPath("$.type").value("MARKET"))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.price").value(103.0))
    }

    @Test
    fun `limit buy order is stored as NEW at the requested price`() {
        val payload = mapOf(
            "userId" to 1,
            "ticker" to "PAMP",
            "side" to "BUY",
            "type" to "LIMIT",
            "size" to 5,
            "price" to 90.0
        )

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.price").value(90.0))
    }

    @Test
    fun `buy with amount converts to whole shares using floor division`() {
        val payload = mapOf(
            "userId" to 1,
            "ticker" to "PAMP",
            "side" to "BUY",
            "type" to "MARKET",
            "amount" to 500
        )

        // Latest close for PAMP is 103.00 → 500 / 103 = 4 shares (floor)
        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("FILLED"))
            .andExpect(jsonPath("$.size").value(4))
    }

    @Test
    fun `buy with insufficient funds is stored as REJECTED`() {
        val payload = mapOf(
            "userId" to 2,
            "ticker" to "YPFD",
            "side" to "BUY",
            "type" to "MARKET",
            "size" to 100000
        )

        val json = mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andReturn().response.contentAsString

        val id = mapper.readTree(json).get("id").asInt()
        assertEquals(OrderStatus.REJECTED, orderRepository.findById(id)?.status)
    }

    @Test
    fun `sell with insufficient shares is stored as REJECTED`() {
        val payload = mapOf(
            "userId" to 2,
            "ticker" to "GGAL",
            "side" to "SELL",
            "type" to "MARKET",
            "size" to 5
        )

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("REJECTED"))
    }

    @Test
    fun `submitting an order missing size and amount fails validation`() {
        val payload = mapOf(
            "userId" to 1,
            "ticker" to "PAMP",
            "side" to "BUY",
            "type" to "MARKET"
        )

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `submitting an order with an unknown side fails validation`() {
        val payload = mapOf(
            "userId" to 1,
            "ticker" to "PAMP",
            "side" to "FLY",
            "type" to "MARKET",
            "size" to 1
        )

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `cash-in credits the user account and cash-out then debits it`() {
        val userId = 2
        val portfolioBefore = mockMvc.perform(get("/users/$userId/portfolio"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val cashBefore = mapper.readTree(portfolioBefore).get("availableCash").decimalValue()

        val cashInPayload = mapOf(
            "userId" to userId,
            "side" to "CASH_IN",
            "type" to "MARKET",
            "size" to 100000
        )
        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cashInPayload))
        ).andExpect(status().isCreated).andExpect(jsonPath("$.status").value("FILLED"))

        val cashOutPayload = mapOf(
            "userId" to userId,
            "side" to "CASH_OUT",
            "type" to "MARKET",
            "size" to 30000
        )
        val response = mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cashOutPayload))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("FILLED"))
            .andReturn().response.contentAsString

        assertNotNull(mapper.readTree(response).get("id"))

        val portfolioAfter = mockMvc.perform(get("/users/$userId/portfolio"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val cashAfter = mapper.readTree(portfolioAfter).get("availableCash").decimalValue()

        assertEquals(cashBefore.add(java.math.BigDecimal(70000)).stripTrailingZeros(), cashAfter.stripTrailingZeros())
    }
}
